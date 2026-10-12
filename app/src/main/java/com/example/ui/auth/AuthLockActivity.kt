package com.example.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.SecurityEventRepository
import com.example.data.security.CryptoHelper
import com.example.domain.model.AuthMethod
import com.example.domain.model.ExpressivePalette
import com.example.domain.model.LockscreenEffect
import com.example.domain.model.ThemeMode
import com.example.platform.biometric.BiometricHelper
import com.example.platform.monitoring.AppLockEngine
import com.example.ui.components.AppIconView
import com.example.ui.components.PatternLockInput
import com.example.ui.components.SecurePinKeypad
import com.example.ui.theme.LockMeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.example.data.repository.IntruderGalleryRepository
import com.example.domain.model.IntruderCapture
import com.example.platform.camera.IntruderCameraManager

class AuthLockActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
    }

    private lateinit var dataStore: SettingsDataStore
    private var targetPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Protect screen contents from screenshots & recent app switcher peek
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
        dataStore = SettingsDataStore(applicationContext)

        AppLockEngine.isAuthScreenVisible = true

        // Intercept back button to navigate to home instead of allowing unauthorized access
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(homeIntent)
                finish()
            }
        })

        setContent {
            val settingsState by dataStore.securitySettings.collectAsState(initial = null)
            val darkTheme = when (settingsState?.themeMode ?: ThemeMode.SYSTEM) {
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val palette = settingsState?.expressivePalette ?: ExpressivePalette.DYNAMIC
            val effect = settingsState?.lockscreenEffect ?: LockscreenEffect.BLUR_GLASS
            val isCompact = settingsState?.compactModeEnabled ?: false

            LockMeTheme(
                darkTheme = darkTheme,
                palette = palette
            ) {
                AuthLockScreen(
                    targetPackage = targetPackage ?: "Protected Application",
                    dataStore = dataStore,
                    lockscreenEffect = effect,
                    isCompact = isCompact,
                    onAuthSuccess = {
                        lifecycleScope.launch {
                            dataStore.resetFailedAttempts()
                        }
                        targetPackage?.let { pkg ->
                            AppLockEngine.onPackageUnlocked(pkg)
                        }
                        finish()
                    },
                    onAuthFailed = {
                        handleFailedAttempt()
                    },
                    onTriggerBiometric = {
                        triggerBiometricPrompt()
                    }
                )
            }
        }

        // Auto prompt biometric / face unlock on launch if enabled
        lifecycleScope.launch {
            val settings = dataStore.securitySettings.first()
            if (settings.biometricEnabled) {
                triggerBiometricPrompt()
            }
        }
    }

    private fun handleFailedAttempt() {
        lifecycleScope.launch {
            val settings = dataStore.securitySettings.first()
            val newCount = settings.failedAttempts + 1
            dataStore.recordFailedAttempt(0L)
            SecurityEventRepository.logEvent(
                eventType = "Unlock Failed",
                details = "Incorrect unlock attempt for ${targetPackage ?: "Protected App"} (Attempt $newCount)",
                isSuccess = false
            )

            if (settings.intruderCaptureEnabled && newCount >= settings.intruderThresholdAttempts) {
                if (IntruderCameraManager.isCameraPermissionGranted(applicationContext)) {
                    IntruderCameraManager.captureIntruderPhoto(
                        context = applicationContext,
                        lifecycleOwner = this@AuthLockActivity,
                        targetPackage = targetPackage ?: "com.example.protected",
                        targetAppName = targetPackage ?: "Protected App",
                        attempts = newCount,
                        onCaptureSaved = { photoFile ->
                            lifecycleScope.launch {
                                IntruderGalleryRepository.recordCapture(
                                    IntruderCapture(
                                        id = photoFile.name,
                                        filePath = photoFile.absolutePath,
                                        timestamp = System.currentTimeMillis(),
                                        targetPackage = targetPackage ?: "com.example.protected",
                                        targetAppName = targetPackage ?: "Protected App",
                                        failedAttempts = newCount
                                    )
                                )
                                SecurityEventRepository.logEvent(
                                    eventType = "Intruder Captured",
                                    details = "Secret photo captured after $newCount failed attempts for $targetPackage",
                                    isSuccess = true
                                )
                            }
                        },
                        onCaptureFailed = { reason ->
                            lifecycleScope.launch {
                                SecurityEventRepository.logEvent(
                                    eventType = "Intruder Capture Failed",
                                    details = "Camera capture could not complete: $reason",
                                    isSuccess = false
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    private fun triggerBiometricPrompt() {
        BiometricHelper.promptBiometric(
            activity = this,
            title = "Unlock Application",
            subtitle = "Verify with biometric / face unlock to open ${targetPackage ?: "app"}",
            negativeButtonText = "Use PIN / Pattern",
            onSuccess = {
                lifecycleScope.launch {
                    dataStore.resetFailedAttempts()
                }
                targetPackage?.let { pkg ->
                    AppLockEngine.onPackageUnlocked(pkg)
                }
                finish()
            },
            onError = { _ ->
                // Biometric cancelled or errored, user continues with PIN/pattern
            },
            onFailed = {
                handleFailedAttempt()
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLockEngine.isAuthScreenVisible = false
    }
}

@Composable
fun AuthLockScreen(
    targetPackage: String,
    dataStore: SettingsDataStore,
    lockscreenEffect: LockscreenEffect,
    isCompact: Boolean,
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit,
    onTriggerBiometric: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var enteredInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var authMethod by remember { mutableStateOf(AuthMethod.PIN) }
    var isBiometricEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val settings = dataStore.securitySettings.first()
        authMethod = settings.authMethod
        isBiometricEnabled = settings.biometricEnabled
    }

    // Dynamic background effects
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val backgroundBrush = when (lockscreenEffect) {
        LockscreenEffect.MINIMAL_OLED -> Brush.linearGradient(listOf(Color.Black, Color(0xFF0A0A0A)))
        LockscreenEffect.AURORA -> Brush.radialGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                MaterialTheme.colorScheme.background
            ),
            radius = 1200f
        )
        LockscreenEffect.CYBER_SHIELD -> Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant
            )
        )
        LockscreenEffect.BLUR_GLASS -> Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Overlay visual decorations
        if (lockscreenEffect == LockscreenEffect.CYBER_SHIELD) {
            val gridColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 48.dp.toPx()
                for (x in 0..(size.width / step).toInt()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x * step, 0f),
                        end = Offset(x * step, size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(size.height / step).toInt()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y * step),
                        end = Offset(size.width, y * step),
                        strokeWidth = 1f
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompact) 16.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 14.dp)
            ) {
                Spacer(modifier = Modifier.height(if (isCompact) 14.dp else 28.dp))

                AppIconView(
                    packageName = targetPackage,
                    isGame = false,
                    modifier = Modifier.size(if (isCompact) 52.dp else 68.dp)
                )

                Text(
                    text = "App Shield Active",
                    style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Authenticate to access this application",
                    style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            if (authMethod == AuthMethod.PIN) {
                SecurePinKeypad(
                    pinLength = 4,
                    enteredPin = enteredInput,
                    errorMessage = errorMessage,
                    isCompact = isCompact,
                    onDigitPress = { digit ->
                        if (enteredInput.length < 4) {
                            val updated = enteredInput + digit
                            enteredInput = updated
                            errorMessage = null
                            if (updated.length == 4) {
                                scope.launch {
                                    val verifier = dataStore.getCredentialVerifier()
                                    if (verifier != null && CryptoHelper.verifyCredential(updated, verifier.first, verifier.second)) {
                                        onAuthSuccess()
                                    } else {
                                        errorMessage = "Incorrect PIN. Try again."
                                        enteredInput = ""
                                        onAuthFailed()
                                    }
                                }
                            }
                        }
                    },
                    onDeletePress = {
                        if (enteredInput.isNotEmpty()) enteredInput = enteredInput.dropLast(1)
                    }
                )
            } else {
                PatternLockInput(
                    errorMessage = errorMessage,
                    isCompact = isCompact,
                    onPatternComplete = { pattern ->
                        scope.launch {
                            val verifier = dataStore.getCredentialVerifier()
                            if (verifier != null && CryptoHelper.verifyCredential(pattern, verifier.first, verifier.second)) {
                                onAuthSuccess()
                            } else {
                                errorMessage = "Incorrect Pattern. Try again."
                                onAuthFailed()
                            }
                        }
                    }
                )
            }

            if (isBiometricEnabled) {
                FilledTonalButton(
                    onClick = onTriggerBiometric,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(bottom = if (isCompact) 8.dp else 16.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock with Biometric / Face Lock")
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
