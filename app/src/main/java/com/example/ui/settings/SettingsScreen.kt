package com.example.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.ExpressivePalette
import com.example.domain.model.ThemeMode
import com.example.platform.permissions.PermissionManager
import com.example.ui.components.SettingsClickableRow
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                    text = "Material 3 Expressive Themes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Theme Mode
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Light / Dark Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            ThemeMode.values().forEachIndexed { index, mode ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.values().size),
                                    onClick = { viewModel.setThemeMode(mode) },
                                    selected = uiState.settings.themeMode == mode,
                                    label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                    }
                }
            }

            // Expressive Color Palettes
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Material You Expressive Palette",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose from dynamic wallpaper colors or hand-crafted Material 3 expressive themes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        ExpressivePalette.values().forEach { palette ->
                            val isSelected = uiState.settings.expressivePalette == palette
                            val previewColor = when (palette) {
                                ExpressivePalette.DYNAMIC -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MaterialTheme.colorScheme.primary else IndigoLightPrimary
                                ExpressivePalette.INDIGO -> IndigoLightPrimary
                                ExpressivePalette.EMERALD -> EmeraldLightPrimary
                                ExpressivePalette.AMBER -> AmberLightPrimary
                                ExpressivePalette.MONOCHROME -> MonoLightPrimary
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.setExpressivePalette(palette) }
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(previewColor)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = palette.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = palette.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.setExpressivePalette(palette) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Haptics & Feedback
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Haptic Vibration Feedback",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Vibrate on keypad clicks and auth transitions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.settings.hapticsEnabled,
                                onCheckedChange = { viewModel.setHaptics(it) }
                            )
                        }
                    }
                }
            }

            // Diagnostics & Permissions
            item {
                Text(
                    text = "System Diagnostics & Protection Health",
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
                        SettingsClickableRow(
                            title = "Permission & Capability Diagnostics",
                            subtitle = if (uiState.permissionStatus.usageAccessGranted) "All required permissions granted" else "Usage Access missing",
                            icon = Icons.Default.HealthAndSafety,
                            trailingText = if (uiState.permissionStatus.usageAccessGranted) "Healthy" else "Action needed",
                            onClick = onNavigateToPermissions
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        SettingsClickableRow(
                            title = "Battery Optimization Exemption",
                            subtitle = if (uiState.permissionStatus.batteryOptimizationIgnored) "Unrestricted background monitoring" else "System may throttle monitoring in background",
                            icon = Icons.Default.BatteryChargingFull,
                            trailingText = if (uiState.permissionStatus.batteryOptimizationIgnored) "Exempt" else "Optimize",
                            onClick = { PermissionManager.openBatteryOptimizationSettings(context) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        SettingsClickableRow(
                            title = "App Notification Settings",
                            subtitle = "Controls persistent service notification",
                            icon = Icons.Default.Notifications,
                            onClick = { PermissionManager.openAppSettings(context) }
                        )
                    }
                }
            }

            // About & Credits
            item {
                Text(
                    text = "About LockMe",
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Column {
                                Text(
                                    text = "LockMe v1.0",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Made with love by Rahul Shah",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Offline-first privacy and app lock utility. No tracking, no ads, no cloud sync. All cryptographic hashes and credentials stay strictly on your local device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = { showAboutDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Privacy Guarantee & Details")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text("LockMe Privacy & Architecture", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "• Zero Cloud Telemetry: LockMe operates 100% offline without network accounts or data harvesting.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• PBKDF2 Cryptography: Passcodes are cryptographically hashed using PBKDF2WithHmacSHA256 with unique device salts.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Android BiometricPrompt: Hardware-backed biometrics are handled strictly by the Android OS; LockMe never accesses raw biometric templates.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Developer Credit: Made with love by Rahul Shah.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
