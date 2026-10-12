package com.example.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.data.preferences.SettingsDataStore
import com.example.domain.model.AppProtection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val settingsDataStore: SettingsDataStore
) {

    val installedAppsFlow: Flow<List<AppProtection>> = combine(
        settingsDataStore.protectedPackages
    ) { (protectedSet) ->
        loadInstalledApps(protectedSet)
    }.flowOn(Dispatchers.IO)

    suspend fun getInstalledAppsList(protectedSet: Set<String>): List<AppProtection> =
        withContext(Dispatchers.IO) {
            loadInstalledApps(protectedSet)
        }

    private fun loadInstalledApps(protectedSet: Set<String>): List<AppProtection> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val ownPackage = context.packageName

        return packages
            .filter { appInfo ->
                // Skip own package
                if (appInfo.packageName == ownPackage) return@filter false
                // Must have launch intent or be a valid launchable app/game
                pm.getLaunchIntentForPackage(appInfo.packageName) != null
            }
            .map { appInfo ->
                val appName = pm.getApplicationLabel(appInfo).toString()
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appInfo.category == ApplicationInfo.CATEGORY_GAME ||
                            (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                } else {
                    (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                }

                val categoryName = when {
                    isGame -> "Game"
                    isSystem -> "System App"
                    else -> "Installed App"
                }

                var versionName = ""
                var installTime = 0L
                try {
                    val pkgInfo = pm.getPackageInfo(appInfo.packageName, 0)
                    versionName = pkgInfo.versionName ?: ""
                    installTime = pkgInfo.firstInstallTime
                } catch (e: Exception) {
                    // Ignore package info read error
                }

                AppProtection(
                    packageName = appInfo.packageName,
                    appName = appName,
                    isProtected = protectedSet.contains(appInfo.packageName),
                    isSystemApp = isSystem,
                    isGame = isGame,
                    categoryName = categoryName,
                    installTime = installTime,
                    versionName = versionName
                )
            }
            .sortedWith(
                compareByDescending<AppProtection> { it.isProtected }
                    .thenBy { it.appName.lowercase() }
            )
    }

    suspend fun toggleProtection(packageName: String, isProtected: Boolean) {
        settingsDataStore.toggleAppProtection(packageName, isProtected)
    }

    suspend fun setProtectedAppsBatch(packages: Set<String>) {
        settingsDataStore.setProtectedAppsBatch(packages)
    }
}
