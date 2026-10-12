package com.example.ui.setup

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.model.PermissionStatus
import com.example.platform.permissions.PermissionManager
import com.example.ui.components.PermissionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onPermissionsComplete: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var permissionStatus by remember { mutableStateOf(PermissionManager.getFullPermissionStatus(context)) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        permissionStatus = PermissionManager.getFullPermissionStatus(context)
    }

    fun refresh() {
        permissionStatus = PermissionManager.getFullPermissionStatus(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Protection Setup",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Check again")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            refresh()
                            onPermissionsComplete()
                        },
                        enabled = permissionStatus.usageAccessGranted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("permissions_continue_btn"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (permissionStatus.usageAccessGranted) "Continue to LockMe" else "Grant Usage Access to Continue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Android System Capabilities",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "LockMe requires Usage Access and optional notifications to observe foreground app switches and shield protected apps instantly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 1. Usage Access (Mandatory)
            item {
                PermissionCard(
                    title = "Usage Access (Required)",
                    description = "Enables detection of foreground apps through UsageStatsManager so LockMe intercepts protected apps and games in real-time.",
                    isGranted = permissionStatus.usageAccessGranted,
                    onEnableClick = {
                        PermissionManager.openUsageAccessSettings(context)
                    }
                )
            }

            // 2. Notification Permission (Android 13+)
            item {
                PermissionCard(
                    title = "Service Notification (Recommended)",
                    description = "Displays the active shield notification in status bar so Android does not kill the background monitoring engine.",
                    isGranted = permissionStatus.notificationsGranted,
                    onEnableClick = {
                        if (context is Activity) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                PermissionManager.openAppSettings(context)
                            }
                        } else {
                            PermissionManager.openAppSettings(context)
                        }
                    }
                )
            }

            // 3. Overlay Permission
            item {
                PermissionCard(
                    title = "Display Over Other Apps (Recommended)",
                    description = "Allows LockMe authentication screen to pop up instantly above opened protected apps and games.",
                    isGranted = permissionStatus.overlayGranted,
                    onEnableClick = {
                        PermissionManager.openOverlaySettings(context)
                    }
                )
            }

            // 4. Battery Optimization
            item {
                PermissionCard(
                    title = "Unrestricted Battery Background",
                    description = "Exempts LockMe from aggressive OEM battery savers so real-time monitoring runs uninterrupted.",
                    isGranted = permissionStatus.batteryOptimizationIgnored,
                    onEnableClick = {
                        PermissionManager.openBatteryOptimizationSettings(context)
                    }
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tip: Once you grant permissions in system settings, returning here automatically activates your real-time security shield.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
