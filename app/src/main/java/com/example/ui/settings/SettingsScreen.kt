package com.example.ui.settings

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.domain.model.LockscreenEffect
import com.example.domain.model.ThemeMode
import com.example.platform.permissions.PermissionManager
import com.example.ui.components.AppLogoIcon
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
    var showRenameDialog by remember { mutableStateOf(false) }
    var newAliasText by remember { mutableStateOf(uiState.settings.customAppAlias) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCustomLogoUri(uri.toString())
        }
    }

    val isCompact = uiState.settings.compactModeEnabled

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Customization",
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
            // 1. DISGUISE, NAME & GALLERY LOGO CUSTOMIZATION
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "App Identity & Camouflage",
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AppLogoIcon(
                                customUriString = uiState.settings.customLogoUri,
                                modifier = Modifier.size(56.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.settings.customAppAlias.ifBlank { "LockMe" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Custom app title & gallery icon",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    newAliasText = uiState.settings.customAppAlias
                                    showRenameDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rename App")
                            }

                            FilledTonalButton(
                                onClick = {
                                    photoPickerLauncher.launch("image/*")
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pick Logo")
                            }
                        }

                        if (uiState.settings.customLogoUri.isNotBlank()) {
                            TextButton(
                                onClick = { viewModel.setCustomLogoUri("") },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Reset to default icon")
                            }
                        }
                    }
                }
            }

            // 2. ANTI-UNINSTALL & HIDE PROTECTION
            item {
                Text(
                    text = "Anti-Uninstall & Stealth Protection",
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Anti-Uninstall (Device Admin)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Locks uninstallation so unauthorized users or intruders cannot remove LockMe",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.permissionStatus.deviceAdminGranted,
                                onCheckedChange = { isChecked ->
                                    if (context is Activity) {
                                        viewModel.toggleDeviceAdmin(context, isChecked)
                                    }
                                }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hide from Launcher (Stealth Mode)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Removes LockMe icon from Android app drawer for discrete protection",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.settings.hideFromLauncher,
                                onCheckedChange = { isChecked ->
                                    viewModel.toggleHideFromLauncher(isChecked)
                                }
                            )
                        }
                    }
                }
            }

            // 3. LOCKSCREEN OVERLAY EFFECTS
            item {
                Text(
                    text = "Lockscreen Overlay & Visual Effects",
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
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LockscreenEffect.values().forEach { effect ->
                            val isSelected = uiState.settings.lockscreenEffect == effect
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setLockscreenEffect(effect) }
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = when (effect) {
                                            LockscreenEffect.BLUR_GLASS -> Icons.Default.BlurOn
                                            LockscreenEffect.CYBER_SHIELD -> Icons.Default.Security
                                            LockscreenEffect.MINIMAL_OLED -> Icons.Default.Contrast
                                            LockscreenEffect.AURORA -> Icons.Default.Gradient
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = effect.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = effect.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.setLockscreenEffect(effect) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. DISPLAY COMPACT MODE & THEMES
            item {
                Text(
                    text = "Display Adaptation & Themes",
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
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Compact Mode for Small Displays
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Compact Layout Mode",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Auto-scales keypad, paddings, and font sizes to comfortably fit small display phones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.settings.compactModeEnabled,
                                onCheckedChange = { viewModel.setCompactMode(it) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "Light / Dark Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

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

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "Material You Expressive Palette",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        ExpressivePalette.values().forEach { palette ->
                            val isSelected = uiState.settings.expressivePalette == palette
                            val previewColor = when (palette) {
                                ExpressivePalette.DYNAMIC -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MaterialTheme.colorScheme.primary else IndigoLightPrimary
                                ExpressivePalette.INDIGO -> IndigoLightPrimary
                                ExpressivePalette.EMERALD -> EmeraldLightPrimary
                                ExpressivePalette.AMBER -> AmberLightPrimary
                                ExpressivePalette.MONOCHROME -> MonoLightPrimary
                                ExpressivePalette.LAVENDER -> LavenderLightPrimary
                                ExpressivePalette.MINT -> MintLightPrimary
                                ExpressivePalette.OCEAN -> OceanLightPrimary
                                ExpressivePalette.MIDNIGHT -> MidnightLightPrimary
                                ExpressivePalette.ROSE -> RoseLightPrimary
                                ExpressivePalette.SAGE -> SageLightPrimary
                                ExpressivePalette.PEACH -> PeachLightPrimary
                                ExpressivePalette.GRAPHITE -> GraphiteLightPrimary
                                ExpressivePalette.AURORA -> AuroraLightPrimary
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setExpressivePalette(palette) }
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(previewColor)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = palette.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
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

            // 5. DIAGNOSTICS & SYSTEM PERMISSIONS
            item {
                Text(
                    text = "System Diagnostics & Capabilities",
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
                    Column(modifier = Modifier.padding(14.dp)) {
                        SettingsClickableRow(
                            title = "Permission & Protection Diagnostics",
                            subtitle = if (uiState.permissionStatus.usageAccessGranted) "Usage Access active" else "Usage Access missing",
                            icon = Icons.Default.HealthAndSafety,
                            trailingText = if (uiState.permissionStatus.usageAccessGranted) "Active" else "Fix",
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

            // About LockMe
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

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename LockMe Display Title", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newAliasText,
                    onValueChange = { newAliasText = it },
                    label = { Text("App Alias") },
                    placeholder = { Text("e.g. Calculator, Notes, Clock") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setCustomAppAlias(newAliasText)
                        showRenameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                        text = "• Anti-Uninstall Device Administrator: Prevents malicious uninstallation through system-level device policy manager.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Camouflage & Stealth: Rename the application and upload custom icons directly from your device gallery.",
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
