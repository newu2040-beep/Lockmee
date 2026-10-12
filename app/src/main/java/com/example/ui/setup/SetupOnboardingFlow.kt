package com.example.ui.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.preferences.SettingsDataStore
import com.example.data.security.CryptoHelper
import com.example.domain.model.AuthMethod
import com.example.platform.biometric.BiometricHelper
import com.example.ui.components.PatternLockInput
import com.example.ui.components.SecurePinKeypad
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupOnboardingFlow(
    dataStore: SettingsDataStore,
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Steps: 1: Welcome, 2: Choose Method, 3: Create PIN/Pattern, 4: Confirm PIN/Pattern, 5: Biometric, 6: Permissions
    var step by remember { mutableStateOf(1) }
    var chosenMethod by remember { mutableStateOf(AuthMethod.PIN) }
    var initialSecret by remember { mutableStateOf("") }
    var confirmSecret by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var enableBiometrics by remember { mutableStateOf(false) }

    val (isBioAvailable, bioMessage) = remember { BiometricHelper.isBiometricAvailable(context) }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (step) {
                // Step 1: Welcome & Introduction
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(54.dp)
                                )
                            }

                            Text(
                                text = "Keep Your Apps Private",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Shield your messages, gallery, banking, and sensitive apps with PIN, pattern, or fingerprint authentication.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "100% Offline: Your credentials and data never leave this device.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { step = 2 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("onboarding_get_started_btn"),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "Get Started",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Step 2: Choose Lock Method
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            IconButton(onClick = { step = 1 }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }

                            Text(
                                text = "Choose Your Lock",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Select your primary passcode mechanism to secure access to protected applications.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            listOf(AuthMethod.PIN, AuthMethod.PATTERN).forEach { method ->
                                val isSelected = chosenMethod == method
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { chosenMethod = method }
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (method == AuthMethod.PIN) Icons.Default.Pin else Icons.Default.Gesture,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = method.displayName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = method.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { chosenMethod = method }
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                initialSecret = ""
                                confirmSecret = ""
                                errorMessage = null
                                step = 3
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("continue_to_create_cred_btn"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Continue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Step 3: Create PIN or Pattern
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                IconButton(onClick = { step = 2 }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }

                            Text(
                                text = if (chosenMethod == AuthMethod.PIN) "Create Your PIN" else "Draw Your Pattern",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = if (chosenMethod == AuthMethod.PIN) "Enter a 4-digit numeric code" else "Connect at least 4 dots",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (chosenMethod == AuthMethod.PIN) {
                            SecurePinKeypad(
                                pinLength = 4,
                                enteredPin = initialSecret,
                                errorMessage = errorMessage,
                                onDigitPress = { digit ->
                                    if (initialSecret.length < 4) {
                                        initialSecret += digit
                                        errorMessage = null
                                        if (initialSecret.length == 4) {
                                            step = 4
                                        }
                                    }
                                },
                                onDeletePress = {
                                    if (initialSecret.isNotEmpty()) initialSecret = initialSecret.dropLast(1)
                                }
                            )
                        } else {
                            PatternLockInput(
                                errorMessage = errorMessage,
                                onPatternComplete = { pattern ->
                                    initialSecret = pattern
                                    step = 4
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Step 4: Confirm PIN or Pattern
                4 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                IconButton(onClick = {
                                    initialSecret = ""
                                    confirmSecret = ""
                                    errorMessage = null
                                    step = 3
                                }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }

                            Text(
                                text = if (chosenMethod == AuthMethod.PIN) "Confirm Your PIN" else "Confirm Your Pattern",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Re-enter the same passcode to confirm",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (chosenMethod == AuthMethod.PIN) {
                            SecurePinKeypad(
                                pinLength = 4,
                                enteredPin = confirmSecret,
                                errorMessage = errorMessage,
                                onDigitPress = { digit ->
                                    if (confirmSecret.length < 4) {
                                        confirmSecret += digit
                                        errorMessage = null
                                        if (confirmSecret.length == 4) {
                                            if (confirmSecret == initialSecret) {
                                                // Save securely
                                                scope.launch {
                                                    val salt = CryptoHelper.generateSalt()
                                                    val hash = CryptoHelper.hashCredential(confirmSecret, salt)
                                                    dataStore.saveCredential(chosenMethod, salt, hash)
                                                    step = 5 // Biometrics step
                                                }
                                            } else {
                                                errorMessage = "PINs do not match. Try again."
                                                confirmSecret = ""
                                            }
                                        }
                                    }
                                },
                                onDeletePress = {
                                    if (confirmSecret.isNotEmpty()) confirmSecret = confirmSecret.dropLast(1)
                                }
                            )
                        } else {
                            PatternLockInput(
                                errorMessage = errorMessage,
                                onPatternComplete = { pattern ->
                                    if (pattern == initialSecret) {
                                        scope.launch {
                                            val salt = CryptoHelper.generateSalt()
                                            val hash = CryptoHelper.hashCredential(pattern, salt)
                                            dataStore.saveCredential(chosenMethod, salt, hash)
                                            step = 5 // Biometrics step
                                        }
                                    } else {
                                        errorMessage = "Patterns do not match. Try again."
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Step 5: Enable Biometrics
                5 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(56.dp)
                                )
                            }

                            Text(
                                text = "Unlock in a Second",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Use fingerprint or face recognition for fast and secure unlocking without typing your PIN every time.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = bioMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isBioAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isBioAvailable) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            dataStore.setBiometricEnabled(true)
                                            step = 6
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("enable_biometrics_btn"),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Enable Biometrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        dataStore.setBiometricEnabled(false)
                                        step = 6
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(if (isBioAvailable) "Maybe Later" else "Skip Biometrics")
                            }
                        }
                    }
                }

                // Step 6: Permission Setup
                6 -> {
                    PermissionsScreen(
                        onPermissionsComplete = onSetupComplete,
                        onBack = null
                    )
                }
            }
        }
    }
}
