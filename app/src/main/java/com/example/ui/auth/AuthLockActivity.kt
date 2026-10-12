package com.example.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.draw.clip
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
import com.example.domain.model.ThemeMode
import com.example.platform.biometric.BiometricHelper
import com.example.platform.monitoring.AppLockEngine
import com.example.ui.components.AppIconView
import com.example.ui.components.PatternLockInput
import com.example.ui.components.SecurePinKeypad
import com.example.ui.theme.LockMeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

            LockMeTheme(
                darkTheme = darkTheme,
                palette = palette
            ) {
                AuthLockScreen(
                    targetPackage = targetPackage ?: "Protected Application",
                    dataStore = dataStore,
                    onAuthSuccess = {
                        targetPackage?.let { pkg ->
                            AppLockEngine.onPackageUnlocked(pkg)
                        }
                        finish()
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

    private fun triggerBiometricPrompt() {
        BiometricHelper.promptBiometric(
            activity = this,
            title = "Unlock Application",
            subtitle = "Verify with biometric / face unlock to open ${targetPackage ?: "app"}",
            negativeButtonText = "Use PIN / Pattern",
            onSuccess = {
                targetPackage?.let { pkg ->
                    AppLockEngine.onPackageUnlocked(pkg)
                }
                finish()
            },
            onError = { _ ->
                // Biometric cancelled or errored, user continues with PIN/pattern
            },
            onFailed = {
                // Biometric mismatch
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
    onAuthSuccess: () -> Unit,
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                AppIconView(
                    packageName = targetPackage,
                    isGame = false,
                    modifier = Modifier.size(68.dp)
                )

                Text(
                    text = "App Shield Active",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Authenticate to access this application",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            if (authMethod == AuthMethod.PIN) {
                SecurePinKeypad(
                    pinLength = 4,
                    enteredPin = enteredInput,
                    errorMessage = errorMessage,
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
                                        dataStore.recordFailedAttempt(0L)
                                        SecurityEventRepository.logEvent(
                                            eventType = "Unlock Failed",
                                            details = "Incorrect PIN attempt for $targetPackage",
                                            isSuccess = false
                                        )
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
                    onPatternComplete = { pattern ->
                        scope.launch {
                            val verifier = dataStore.getCredentialVerifier()
                            if (verifier != null && CryptoHelper.verifyCredential(pattern, verifier.first, verifier.second)) {
                                onAuthSuccess()
                            } else {
                                errorMessage = "Incorrect Pattern. Try again."
                                dataStore.recordFailedAttempt(0L)
                                SecurityEventRepository.logEvent(
                                    eventType = "Unlock Failed",
                                    details = "Incorrect pattern attempt for $targetPackage",
                                    isSuccess = false
                                )
                            }
                        }
                    }
                )
            }

            if (isBiometricEnabled) {
                FilledTonalButton(
                    onClick = onTriggerBiometric,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
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
