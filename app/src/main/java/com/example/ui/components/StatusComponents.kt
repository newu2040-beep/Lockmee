package com.example.ui.components

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.domain.model.AppProtection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppIconView(
    packageName: String,
    isGame: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var iconBitmap by remember(packageName) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(packageName) {
        withContext(Dispatchers.IO) {
            try {
                val drawable = context.packageManager.getApplicationIcon(packageName)
                val bitmap = drawable.toBitmap(128, 128)
                iconBitmap = bitmap.asImageBitmap()
            } catch (e: Exception) {
                iconBitmap = null
            }
        }
    }

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = if (isGame) Icons.Default.SportsEsports else Icons.Default.Apps,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ProtectionStatusCard(
    isProtected: Boolean,
    needsSetup: Boolean,
    onToggleProtection: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            needsSetup -> MaterialTheme.colorScheme.errorContainer
            isProtected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "statusCardColor"
    )

    val contentColor = when {
        needsSetup -> MaterialTheme.colorScheme.onErrorContainer
        isProtected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("protection_status_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    needsSetup -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    isProtected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                needsSetup -> Icons.Default.Warning
                                isProtected -> Icons.Default.Shield
                                else -> Icons.Default.LockOpen
                            },
                            contentDescription = "Shield Status",
                            tint = contentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "App & Game Shield",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                        Text(
                            text = when {
                                needsSetup -> "Action Required: Setup permissions"
                                isProtected -> "Real-time Monitoring Active"
                                else -> "Protection Paused"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = 0.85f)
                        )
                    }
                }

                if (!needsSetup) {
                    Switch(
                        checked = isProtected,
                        onCheckedChange = onToggleProtection,
                        modifier = Modifier.testTag("protection_toggle_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = when {
                    needsSetup -> "Grant Usage Access in Settings so LockMe can detect when protected apps or games are launched in real-time."
                    isProtected -> "Foreground app transitions are observed in real-time using UsageStatsManager and intercepted with native biometric/PIN verification."
                    else -> "LockMe monitoring is temporarily paused. Tap the switch to resume full defense."
                },
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun ProtectedAppRow(
    app: AppProtection,
    onToggle: (Boolean) -> Unit,
    onAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onAppClick)
            .testTag("app_row_${app.packageName}"),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                AppIconView(
                    packageName = app.packageName,
                    isGame = app.isGame
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (app.isGame) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ) {
                                Text("GAME", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Text(
                        text = "${app.categoryName} • ${if (app.versionName.isNotEmpty()) "v${app.versionName}" else app.packageName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Switch(
                checked = app.isProtected,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("app_switch_${app.packageName}")
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
