package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.lifecycleScope
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.AppRepository
import com.example.domain.model.ExpressivePalette
import com.example.domain.model.ThemeMode
import com.example.platform.monitoring.AppLockEngine
import com.example.platform.monitoring.LockMeForegroundService
import com.example.platform.permissions.PermissionManager
import com.example.ui.apps.AppsScreen
import com.example.ui.apps.AppsViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.security.SecurityScreen
import com.example.ui.security.SecurityViewModel
import com.example.ui.intruders.IntruderGalleryScreen
import com.example.ui.intruders.IntruderViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.setup.PermissionsScreen
import com.example.ui.setup.SetupOnboardingFlow
import com.example.ui.theme.LockMeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class MainDestination(val label: String) {
    HOME("Home"),
    APPS("Apps"),
    SECURITY("Security"),
    SETTINGS("Settings"),
    PERMISSIONS("Permissions"),
    INTRUDER_GALLERY("Intruders")
}

class MainActivity : AppCompatActivity() {

    private lateinit var dataStore: SettingsDataStore
    private lateinit var appRepository: AppRepository

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(applicationContext, dataStore, appRepository)
    }

    private val appsViewModel: AppsViewModel by viewModels {
        AppsViewModel.Factory(appRepository, dataStore)
    }

    private val securityViewModel: SecurityViewModel by viewModels {
        SecurityViewModel.Factory(applicationContext, dataStore)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(applicationContext, dataStore)
    }

    private val intruderViewModel: IntruderViewModel by viewModels {
        IntruderViewModel.Factory(applicationContext, dataStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dataStore = SettingsDataStore(applicationContext)
        appRepository = AppRepository(applicationContext, dataStore)

        setContent {
            val settingsState by dataStore.securitySettings.collectAsState(initial = null)

            val darkTheme = when (settingsState?.themeMode ?: ThemeMode.SYSTEM) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val palette = settingsState?.expressivePalette ?: ExpressivePalette.DYNAMIC

            LockMeTheme(
                darkTheme = darkTheme,
                palette = palette
            ) {
                if (settingsState == null) {
                    Surface(modifier = Modifier.fillMaxSize()) {}
                } else if (!settingsState!!.isSetupComplete) {
                    SetupOnboardingFlow(
                        dataStore = dataStore,
                        onSetupComplete = {
                            if (PermissionManager.checkUsageAccess(applicationContext)) {
                                LockMeForegroundService.startService(applicationContext)
                                AppLockEngine.startMonitoring(applicationContext, dataStore)
                            }
                        }
                    )
                } else {
                    MainAppScaffold(
                        homeViewModel = homeViewModel,
                        appsViewModel = appsViewModel,
                        securityViewModel = securityViewModel,
                        settingsViewModel = settingsViewModel,
                        intruderViewModel = intruderViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.refreshState()
        settingsViewModel.refreshStatus()

        // Auto restart monitoring if configured
        lifecycleScope.launch {
            val settings = dataStore.securitySettings.first()
            if (settings.isSetupComplete && !settings.protectionPaused && PermissionManager.checkUsageAccess(applicationContext)) {
                LockMeForegroundService.startService(applicationContext)
                AppLockEngine.startMonitoring(applicationContext, dataStore)
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    homeViewModel: HomeViewModel,
    appsViewModel: AppsViewModel,
    securityViewModel: SecurityViewModel,
    settingsViewModel: SettingsViewModel,
    intruderViewModel: IntruderViewModel
) {
    var currentDestination by remember { mutableStateOf(MainDestination.HOME) }

    Scaffold(
        bottomBar = {
            if (currentDestination != MainDestination.PERMISSIONS && currentDestination != MainDestination.INTRUDER_GALLERY) {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentDestination == MainDestination.HOME,
                        onClick = { currentDestination = MainDestination.HOME },
                        icon = {
                            Icon(
                                if (currentDestination == MainDestination.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = currentDestination == MainDestination.APPS,
                        onClick = { currentDestination = MainDestination.APPS },
                        icon = {
                            Icon(
                                if (currentDestination == MainDestination.APPS) Icons.Filled.Apps else Icons.Outlined.Apps,
                                contentDescription = "Apps"
                            )
                        },
                        label = { Text("Apps & Games") },
                        modifier = Modifier.testTag("nav_item_apps")
                    )

                    NavigationBarItem(
                        selected = currentDestination == MainDestination.SECURITY,
                        onClick = { currentDestination = MainDestination.SECURITY },
                        icon = {
                            Icon(
                                if (currentDestination == MainDestination.SECURITY) Icons.Filled.Security else Icons.Outlined.Security,
                                contentDescription = "Security"
                            )
                        },
                        label = { Text("Security") },
                        modifier = Modifier.testTag("nav_item_security")
                    )

                    NavigationBarItem(
                        selected = currentDestination == MainDestination.SETTINGS,
                        onClick = { currentDestination = MainDestination.SETTINGS },
                        icon = {
                            Icon(
                                if (currentDestination == MainDestination.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentDestination) {
            MainDestination.HOME -> {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToApps = { currentDestination = MainDestination.APPS },
                    onNavigateToSecurity = { currentDestination = MainDestination.SECURITY },
                    onNavigateToPermissions = { currentDestination = MainDestination.PERMISSIONS },
                    onNavigateToSettings = { currentDestination = MainDestination.SETTINGS },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            MainDestination.APPS -> {
                AppsScreen(
                    viewModel = appsViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            MainDestination.SECURITY -> {
                SecurityScreen(
                    viewModel = securityViewModel,
                    onNavigateToIntruderGallery = { currentDestination = MainDestination.INTRUDER_GALLERY },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            MainDestination.SETTINGS -> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToPermissions = { currentDestination = MainDestination.PERMISSIONS },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            MainDestination.PERMISSIONS -> {
                PermissionsScreen(
                    onPermissionsComplete = {
                        currentDestination = MainDestination.HOME
                    },
                    onBack = {
                        currentDestination = MainDestination.HOME
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            MainDestination.INTRUDER_GALLERY -> {
                IntruderGalleryScreen(
                    viewModel = intruderViewModel,
                    onBack = {
                        currentDestination = MainDestination.SECURITY
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
