package com.example.ui.security

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.SecurityEventRepository
import com.example.data.security.CryptoHelper
import com.example.domain.model.AuthMethod
import com.example.domain.model.RelockPolicy
import com.example.domain.model.UserSecuritySettings
import com.example.platform.biometric.BiometricHelper
import com.example.platform.camera.IntruderCameraManager
import com.example.platform.localization.appString
import com.example.ui.components.SecurePinKeypad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    viewModel: SecurityViewModel,
    onNavigateToIntruderGallery: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.checkHardware()
        if (isGranted) {
            viewModel.setIntruderDetectionEnabled(true)
        }
    }

    var showChangePinModal by remember { mutableStateOf(false) }
    var changeStep by remember { mutableStateOf(1) }
    var enteredCurrentPin by remember { mutableStateOf("") }
    var enteredNewPin by remember { mutableStateOf("") }
    var enteredConfirmPin by remember { mutableStateOf("") }
    var pinModalError by remember { mutableStateOf<String?>(null) }

    val isCompact = uiState.settings.compactModeEnabled

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = appString("security_center"),
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
                .padding(horizontal = if (isCompact) 12.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 16.dp)
        ) {
            // 1. CREDENTIAL PASSCODE CARD
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Authentication Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
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
                                    text = "Encrypted with PBKDF2 Hmac-SHA256 & unique device salt",
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
                            Text(appString("change_pin"))
                        }
                    }
                }
            }

            // 2. INTRUDER DETECTION
            item {
                Text(
                    text = appString("intruder_detection"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = "Intruder Secret Selfie",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = appString("intruder_desc"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.settings.intruderCaptureEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        if (uiState.isCameraPermissionGranted) {
                                            viewModel.setIntruderDetectionEnabled(true)
                                        } else {
                                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                        }
                                    } else {
                                        viewModel.setIntruderDetectionEnabled(false)
                                    }
                                }
                            )
                        }

                        if (uiState.settings.intruderCaptureEnabled) {
                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Capture after attempts:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                SingleChoiceSegmentedButtonRow {
                                    listOf(1, 2, 3, 5).forEachIndexed { index, threshold ->
                                        SegmentedButton(
                                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 4),
                                            selected = uiState.settings.intruderThresholdAttempts == threshold,
                                            onClick = { viewModel.setIntruderThreshold(threshold) },
                                            label = { Text("$threshold") }
                                        )
                                    }
                                }
                            }

                            FilledTonalButton(
                                onClick = onNavigateToIntruderGallery,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("View Intruder Photo Gallery")
                            }
                        }
                    }
                }
            }

            // 3. BIOMETRICS SECTION
            item {
                Text(
                    text = appString("biometrics"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
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
                                        text = "Biometric / Face Prompt",
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

            // 4. CUSTOM RELOCK TIMER & SLIDER
            item {
                Text(
                    text = appString("relock_timer"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val currentSeconds = if (uiState.settings.relockPolicy == RelockPolicy.CUSTOM) {
                            uiState.settings.customRelockSeconds
                        } else {
                            when (uiState.settings.relockPolicy) {
                                RelockPolicy.IMMEDIATE -> 0
                                RelockPolicy.GRACE_15S -> 15
                                RelockPolicy.GRACE_30S -> 30
                                RelockPolicy.GRACE_1M -> 60
                                RelockPolicy.GRACE_3M -> 180
                                RelockPolicy.GRACE_5M -> 300
                                RelockPolicy.GRACE_10M -> 600
                                RelockPolicy.SCREEN_OFF -> -1
                                else -> 0
                            }
                        }

                        val timeDisplay = when {
                            currentSeconds == 0 -> "Immediately on leave"
                            currentSeconds == -1 -> "When screen turns off"
                            currentSeconds < 60 -> "$currentSeconds seconds grace"
                            else -> "${currentSeconds / 60}m ${if (currentSeconds % 60 > 0) "${currentSeconds % 60}s" else ""} grace"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected Delay:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Text(timeDisplay, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(4.dp))
                            }
                        }

                        // Material 3 Expressive Slider (0s to 600s / 10 minutes)
                        Slider(
                            value = if (currentSeconds < 0) 0f else currentSeconds.toFloat(),
                            onValueChange = { secondsFloat ->
                                viewModel.setCustomRelockSeconds(secondsFloat.toInt())
                            },
                            valueRange = 0f..600f,
                            steps = 19, // In 30-second steps
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Quick Presets:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Quick Presets Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val presets = listOf(
                                Triple("Immediately", RelockPolicy.IMMEDIATE, 0),
                                Triple("15 seconds", RelockPolicy.GRACE_15S, 15),
                                Triple("30 seconds", RelockPolicy.GRACE_30S, 30),
                                Triple("1 minute", RelockPolicy.GRACE_1M, 60),
                                Triple("3 minutes", RelockPolicy.GRACE_3M, 180),
                                Triple("5 minutes", RelockPolicy.GRACE_5M, 300),
                                Triple("10 minutes", RelockPolicy.GRACE_10M, 600),
                                Triple("Screen Off", RelockPolicy.SCREEN_OFF, -1)
                            )
                            presets.forEach { (label, policy, _) ->
                                val isSelected = uiState.settings.relockPolicy == policy
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setRelockPolicy(policy) },
                                    label = { Text(label) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null
                                )
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
                    Text(appString("cancel"))
                }
            }
        )
    }
}
