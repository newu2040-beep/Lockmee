package com.example.ui.security

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.AuthMethod
import com.example.domain.model.RelockPolicy
import com.example.ui.components.SecurePinKeypad
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    viewModel: SecurityViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showChangePinModal by remember { mutableStateOf(false) }
    var changeStep by remember { mutableStateOf(1) } // 1: Enter current, 2: Enter new, 3: Confirm new
    var enteredCurrentPin by remember { mutableStateOf("") }
    var enteredNewPin by remember { mutableStateOf("") }
    var enteredConfirmPin by remember { mutableStateOf("") }
    var pinModalError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Security Center",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Authentication Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Current Credential Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = when (uiState.settings.authMethod) {
                                    AuthMethod.PIN -> Icons.Default.Pin
                                    AuthMethod.PATTERN -> Icons.Default.Gesture
                                    AuthMethod.PASSWORD -> Icons.Default.Password
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current Method: ${uiState.settings.authMethod.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Protected with PBKDF2 cryptographic hashing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        FilledTonalButton(
                            onClick = {
                                changeStep = 1
                                enteredCurrentPin = ""
                                enteredNewPin = ""
                                enteredConfirmPin = ""
                                pinModalError = null
                                showChangePinModal = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("change_pin_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change PIN Passcode")
                        }
                    }
                }
            }

            // Biometrics Section
            item {
                Text(
                    text = "Biometric Verification",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = "Biometric Prompt",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = uiState.biometricStatusMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.settings.biometricEnabled,
                                enabled = uiState.isBiometricHardwareAvailable,
                                onCheckedChange = { viewModel.setBiometricsEnabled(it) },
                                modifier = Modifier.testTag("biometric_switch")
                            )
                        }
                    }
                }
            }

            // Relocking Policy Section
            item {
                Text(
                    text = "Relocking Behavior & Grace Period",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        RelockPolicy.values().forEach { policy ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setRelockPolicy(policy) }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    RadioButton(
                                        selected = uiState.settings.relockPolicy == policy,
                                        onClick = { viewModel.setRelockPolicy(policy) },
                                        modifier = Modifier.testTag("policy_radio_${policy.name}")
                                    )
                                    Column {
                                        Text(
                                            text = policy.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = when (policy) {
                                                RelockPolicy.IMMEDIATE -> "Locks immediately when exiting the protected app"
                                                RelockPolicy.GRACE_30S -> "Temporary 30 seconds pass before requiring re-auth"
                                                RelockPolicy.GRACE_1M -> "Temporary 60 seconds pass for frequent app switching"
                                                RelockPolicy.GRACE_5M -> "Temporary 5 minutes pass for extended reading"
                                                RelockPolicy.SCREEN_OFF -> "Keeps unlocked until device screen turns off"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Change PIN Dialog
    if (showChangePinModal) {
        AlertDialog(
            onDismissRequest = { showChangePinModal = false },
            title = {
                Text(
                    text = when (changeStep) {
                        1 -> "Verify Current PIN"
                        2 -> "Create New 4-Digit PIN"
                        else -> "Confirm New PIN"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val currentInput = when (changeStep) {
                        1 -> enteredCurrentPin
                        2 -> enteredNewPin
                        else -> enteredConfirmPin
                    }

                    SecurePinKeypad(
                        pinLength = 4,
                        enteredPin = currentInput,
                        errorMessage = pinModalError,
                        onDigitPress = { digit ->
                            if (currentInput.length < 4) {
                                val updated = currentInput + digit
                                pinModalError = null
                                when (changeStep) {
                                    1 -> {
                                        enteredCurrentPin = updated
                                        if (updated.length == 4) {
                                            scope.launch {
                                                if (viewModel.verifyCurrentCredential(updated)) {
                                                    changeStep = 2
                                                } else {
                                                    pinModalError = "Incorrect current PIN"
                                                    enteredCurrentPin = ""
                                                }
                                            }
                                        }
                                    }
                                    2 -> {
                                        enteredNewPin = updated
                                        if (updated.length == 4) {
                                            changeStep = 3
                                        }
                                    }
                                    3 -> {
                                        enteredConfirmPin = updated
                                        if (updated.length == 4) {
                                            if (updated == enteredNewPin) {
                                                scope.launch {
                                                    viewModel.updateCredential(AuthMethod.PIN, updated)
                                                    showChangePinModal = false
                                                }
                                            } else {
                                                pinModalError = "PINs do not match. Try again."
                                                enteredConfirmPin = ""
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        onDeletePress = {
                            when (changeStep) {
                                1 -> if (enteredCurrentPin.isNotEmpty()) enteredCurrentPin = enteredCurrentPin.dropLast(1)
                                2 -> if (enteredNewPin.isNotEmpty()) enteredNewPin = enteredNewPin.dropLast(1)
                                3 -> if (enteredConfirmPin.isNotEmpty()) enteredConfirmPin = enteredConfirmPin.dropLast(1)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showChangePinModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
