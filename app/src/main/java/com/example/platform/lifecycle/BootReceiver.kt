package com.example.platform.lifecycle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.SecurityEventRepository
import com.example.platform.monitoring.AppLockEngine
import com.example.platform.monitoring.LockMeForegroundService
import com.example.platform.permissions.PermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val dataStore = SettingsDataStore(context)
            CoroutineScope(Dispatchers.Default).launch {
                val settings = dataStore.securitySettings.first()
                if (settings.isSetupComplete && !settings.protectionPaused && PermissionManager.checkUsageAccess(context)) {
                    LockMeForegroundService.startService(context)
                    AppLockEngine.startMonitoring(context, dataStore)
                    SecurityEventRepository.logEvent(
                        eventType = "Device Reboot Recovery",
                        details = "Protection engine restarted after system reboot",
                        isSuccess = true
                    )
                }
            }
        }
    }
}
