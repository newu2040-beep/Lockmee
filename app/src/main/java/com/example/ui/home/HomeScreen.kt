package com.example.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppLogoIcon
import com.example.ui.components.ProtectionStatusCard
import com.example.ui.components.SecurityEventRow
import com.example.ui.components.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToApps: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val needsSetup = !uiState.permissionStatus.usageAccessGranted
    val isCompact = uiState.settings.compactModeEnabled

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppLogoIcon(
                            customUriString = uiState.settings.customLogoUri,
                            modifier = Modifier.size(34.dp)
                        )
                        Column {
                            Text(
                                text = uiState.settings.customAppAlias.ifBlank { "LockMe" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.settings.deviceAdminEnabled) {
                                Text(
                                    text = "Anti-Uninstall Protected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("home_settings_btn")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
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
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                // Protection status banner
                ProtectionStatusCard(
                    isProtected = !uiState.settings.protectionPaused && !needsSetup,
                    needsSetup = needsSetup,
                    onToggleProtection = { viewModel.toggleProtection(it) }
                )
            }

            // Real telemetry & statistics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)
                ) {
                    StatCard(
                        title = "Shielded",
                        value = "${uiState.protectedAppsCount}",
                        icon = Icons.Default.Lock,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Method",
                        value = uiState.settings.authMethod.name,
                        icon = Icons.Default.Key,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Status",
                        value = if (uiState.isMonitoringActive && !uiState.settings.protectionPaused) "Active" else "Idle",
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick actions
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onNavigateToApps,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_protect_apps_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apps & Games")
                    }

                    FilledTonalButton(
                        onClick = onNavigateToSecurity,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_security_settings_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Security")
                    }
                }
            }

            if (needsSetup) {
                item {
                    Button(
                        onClick = onNavigateToPermissions,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fix_permissions_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grant Permissions to Activate")
                    }
                }
            }

            // Recent activity header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Real-Time Audit Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (uiState.recentEvents.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearEvents() }) {
                            Text("Clear")
                        }
                    }
                }
            }

            if (uiState.recentEvents.isEmpty()) {
                item {
                    Text(
                        text = "No recent events recorded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                items(uiState.recentEvents, key = { it.id }) { event ->
                    SecurityEventRow(event = event)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
