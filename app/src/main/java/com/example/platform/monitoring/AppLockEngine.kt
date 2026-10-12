package com.example.platform.monitoring

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.SecurityEventRepository
import com.example.domain.model.RelockPolicy
import com.example.domain.model.UserSecuritySettings
import com.example.ui.auth.AuthLockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object AppLockEngine {
    private var monitorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    // Track recently unlocked packages with timestamp of unlock
    private val unlockedPackages = ConcurrentHashMap<String, Long>()
    
    // Track the current foreground package to avoid duplicate launches
    @Volatile
    private var lastForegroundPackage: String? = null

    // Whether auth activity is currently visible
    @Volatile
    var isAuthScreenVisible: Boolean = false

    fun startMonitoring(context: Context, dataStore: SettingsDataStore) {
        if (monitorJob != null && monitorJob?.isActive == true) return

        monitorJob = scope.launch {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return@launch

            while (isActive) {
                try {
                    val settings = dataStore.securitySettings.first()
                    if (!settings.isSetupComplete || settings.protectionPaused) {
                        delay(1500)
                        continue
                    }

                    val protectedPkgs = dataStore.protectedPackages.first()
                    if (protectedPkgs.isEmpty()) {
                        delay(1500)
                        continue
                    }

                    val currentForeground = getForegroundPackage(usageStatsManager)
                    
                    if (currentForeground != null && currentForeground != context.packageName) {
                        if (protectedPkgs.contains(currentForeground)) {
                            // Check if this package is currently unlocked within grace period
                            val isUnlocked = isPackageUnlocked(currentForeground, settings)

                            if (!isUnlocked && !isAuthScreenVisible && currentForeground != lastForegroundPackage) {
                                // Need to trigger lock screen for this package!
                                lastForegroundPackage = currentForeground
                                launchLockScreen(context, currentForeground)
                            }
                        } else {
                            // Switched to an unprotected app
                            lastForegroundPackage = currentForeground
                        }
                    } else if (currentForeground == context.packageName) {
                        lastForegroundPackage = currentForeground
                    }

                } catch (e: Exception) {
                    // Fail-safe protection loop
                }

                delay(350) // 350ms check interval for high-responsiveness
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    fun isMonitoringActive(): Boolean {
        return monitorJob?.isActive == true
    }

    private fun getForegroundPackage(usageStatsManager: UsageStatsManager): String? {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 10000 // 10 seconds window

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime) ?: return null
        val event = UsageEvents.Event()
        var lastPkg: String? = null
        var lastTimestamp = 0L

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (event.timeStamp >= lastTimestamp) {
                    lastTimestamp = event.timeStamp
                    lastPkg = event.packageName
                }
            }
        }
        return lastPkg
    }

    private fun isPackageUnlocked(packageName: String, settings: UserSecuritySettings): Boolean {
        val unlockTime = unlockedPackages[packageName] ?: return false
        val now = System.currentTimeMillis()

        return when (settings.relockPolicy) {
            RelockPolicy.IMMEDIATE -> false
            RelockPolicy.CUSTOM -> (now - unlockTime) < (settings.customRelockSeconds * 1000L)
            RelockPolicy.GRACE_15S -> (now - unlockTime) < 15_000L
            RelockPolicy.GRACE_30S -> (now - unlockTime) < 30_000L
            RelockPolicy.GRACE_1M -> (now - unlockTime) < 60_000L
            RelockPolicy.GRACE_3M -> (now - unlockTime) < 180_000L
            RelockPolicy.GRACE_5M -> (now - unlockTime) < 300_000L
            RelockPolicy.GRACE_10M -> (now - unlockTime) < 600_000L
            RelockPolicy.SCREEN_OFF -> true
        }
    }

    fun onPackageUnlocked(packageName: String) {
        unlockedPackages[packageName] = System.currentTimeMillis()
        SecurityEventRepository.logEvent(
            eventType = "App Unlocked",
            details = "Unlocked access to $packageName",
            isSuccess = true
        )
    }

    fun onScreenOff() {
        unlockedPackages.clear()
        lastForegroundPackage = null
    }

    fun clearAllUnlocks() {
        unlockedPackages.clear()
        lastForegroundPackage = null
    }

    private fun launchLockScreen(context: Context, targetPackage: String) {
        val intent = Intent(context, AuthLockActivity::class.java).apply {
            putExtra(AuthLockActivity.EXTRA_TARGET_PACKAGE, targetPackage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Error starting lock screen
        }
    }
}
