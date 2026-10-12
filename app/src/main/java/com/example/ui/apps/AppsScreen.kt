package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppCategory
import com.example.platform.permissions.PermissionManager
import com.example.ui.components.AppIconView
import com.example.ui.components.ProtectedAppRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    viewModel: AppsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Apps & Games",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.protectedCount} protected • ${uiState.gamesCount} games • ${uiState.regularAppsCount} apps",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val allFilteredProtected = uiState.filteredApps.all { it.isProtected }
                            if (allFilteredProtected) {
                                viewModel.unprotectAllFiltered()
                            } else {
                                viewModel.protectAllFiltered()
                            }
                        },
                        modifier = Modifier.testTag("apps_select_all_btn")
                    ) {
                        val allFilteredProtected = uiState.filteredApps.isNotEmpty() && uiState.filteredApps.all { it.isProtected }
                        Icon(
                            imageVector = if (allFilteredProtected) Icons.Default.Deselect else Icons.Default.SelectAll,
                            contentDescription = "Select or deselect filtered"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search apps and games...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("apps_search_input")
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.activeCategory == AppCategory.ALL,
                    onClick = { viewModel.onCategoryChanged(AppCategory.ALL) },
                    label = { Text("All (${uiState.allApps.size})") },
                    modifier = Modifier.testTag("filter_chip_all")
                )
                FilterChip(
                    selected = uiState.activeCategory == AppCategory.GAMES,
                    onClick = { viewModel.onCategoryChanged(AppCategory.GAMES) },
                    leadingIcon = {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    label = { Text("Games (${uiState.gamesCount})") },
                    modifier = Modifier.testTag("filter_chip_games")
                )
                FilterChip(
                    selected = uiState.activeCategory == AppCategory.APPS,
                    onClick = { viewModel.onCategoryChanged(AppCategory.APPS) },
                    label = { Text("Apps (${uiState.regularAppsCount})") },
                    modifier = Modifier.testTag("filter_chip_apps")
                )
                FilterChip(
                    selected = uiState.activeCategory == AppCategory.PROTECTED,
                    onClick = { viewModel.onCategoryChanged(AppCategory.PROTECTED) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    label = { Text("Shielded (${uiState.protectedCount})") },
                    modifier = Modifier.testTag("filter_chip_protected")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "No apps or games found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Try adjusting your search query or filter selection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.filteredApps,
                        key = { it.packageName }
                    ) { app ->
                        ProtectedAppRow(
                            app = app,
                            onToggle = { isChecked ->
                                viewModel.toggleAppProtection(app.packageName, isChecked)
                            },
                            onAppClick = {
                                viewModel.selectAppForDetails(app)
                            }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // App Details Bottom Sheet
    uiState.selectedAppForDetails?.let { selectedApp ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectAppForDetails(null) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AppIconView(
                        packageName = selectedApp.packageName,
                        isGame = selectedApp.isGame,
                        modifier = Modifier.size(56.dp)
                    )
                    Column {
                        Text(
                            text = selectedApp.appName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedApp.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${selectedApp.categoryName} • ${if (selectedApp.versionName.isNotEmpty()) "v${selectedApp.versionName}" else "Installed"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LockMe Protection Shield",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (selectedApp.isProtected) "Active: Intercepts app launch in real-time" else "Protection currently disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = selectedApp.isProtected,
                        onCheckedChange = {
                            viewModel.toggleAppProtection(selectedApp.packageName, it)
                            viewModel.selectAppForDetails(selectedApp.copy(isProtected = it))
                        }
                    )
                }

                if (selectedApp.installTime > 0L) {
                    val dateFormatted = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(selectedApp.installTime))
                    Text(
                        text = "First Installed: $dateFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = {
                        PermissionManager.openAppSettings(context, selectedApp.packageName)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Android System App Info")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
