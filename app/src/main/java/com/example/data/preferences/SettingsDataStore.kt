package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "lockme_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val KEY_SETUP_COMPLETE = booleanPreferencesKey("is_setup_complete")
        private val KEY_AUTH_METHOD = stringPreferencesKey("auth_method")
        private val KEY_SALT = stringPreferencesKey("cred_salt")
        private val KEY_HASH = stringPreferencesKey("cred_hash")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val KEY_RELOCK_POLICY = stringPreferencesKey("relock_policy")
        private val KEY_CUSTOM_RELOCK_SECONDS = intPreferencesKey("custom_relock_seconds")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_EXPRESSIVE_PALETTE = stringPreferencesKey("expressive_palette")
        private val KEY_LOCKSCREEN_EFFECT = stringPreferencesKey("lockscreen_effect")
        private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_COMPACT_MODE = booleanPreferencesKey("compact_mode")
        private val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")
        private val KEY_PROTECTION_PAUSED = booleanPreferencesKey("protection_paused")
        private val KEY_FAILED_ATTEMPTS = intPreferencesKey("failed_attempts")
        private val KEY_LOCKOUT_UNTIL = longPreferencesKey("lockout_until")
        private val KEY_PROTECTED_PACKAGES = stringSetPreferencesKey("protected_packages")
        private val KEY_FAVORITE_PACKAGES = stringSetPreferencesKey("favorite_packages")
        private val KEY_INTRUDER_ENABLED = booleanPreferencesKey("intruder_enabled")
        private val KEY_INTRUDER_THRESHOLD = intPreferencesKey("intruder_threshold")
        private val KEY_DEVICE_ADMIN = booleanPreferencesKey("device_admin_enabled")
        private val KEY_HIDE_LAUNCHER = booleanPreferencesKey("hide_from_launcher")
        private val KEY_CUSTOM_APP_ALIAS = stringPreferencesKey("custom_app_alias")
        private val KEY_CUSTOM_LOGO_URI = stringPreferencesKey("custom_logo_uri")
    }

    val securitySettings: Flow<UserSecuritySettings> = context.dataStore.data.map { prefs ->
        UserSecuritySettings(
            isSetupComplete = prefs[KEY_SETUP_COMPLETE] ?: false,
            authMethod = try {
                AuthMethod.valueOf(prefs[KEY_AUTH_METHOD] ?: AuthMethod.PIN.name)
            } catch (e: Exception) {
                AuthMethod.PIN
            },
            biometricEnabled = prefs[KEY_BIOMETRIC_ENABLED] ?: false,
            relockPolicy = try {
                RelockPolicy.valueOf(prefs[KEY_RELOCK_POLICY] ?: RelockPolicy.IMMEDIATE.name)
            } catch (e: Exception) {
                RelockPolicy.IMMEDIATE
            },
            customRelockSeconds = prefs[KEY_CUSTOM_RELOCK_SECONDS] ?: 0,
            themeMode = try {
                ThemeMode.valueOf(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            },
            expressivePalette = try {
                ExpressivePalette.valueOf(prefs[KEY_EXPRESSIVE_PALETTE] ?: ExpressivePalette.DYNAMIC.name)
            } catch (e: Exception) {
                ExpressivePalette.DYNAMIC
            },
            lockscreenEffect = try {
                LockscreenEffect.valueOf(prefs[KEY_LOCKSCREEN_EFFECT] ?: LockscreenEffect.BLUR_GLASS.name)
            } catch (e: Exception) {
                LockscreenEffect.BLUR_GLASS
            },
            appLanguage = try {
                AppLanguage.valueOf(prefs[KEY_APP_LANGUAGE] ?: AppLanguage.ENGLISH.name)
            } catch (e: Exception) {
                AppLanguage.ENGLISH
            },
            dynamicColorEnabled = prefs[KEY_DYNAMIC_COLOR] ?: true,
            compactModeEnabled = prefs[KEY_COMPACT_MODE] ?: false,
            hapticsEnabled = prefs[KEY_HAPTICS] ?: true,
            protectionPaused = prefs[KEY_PROTECTION_PAUSED] ?: false,
            failedAttempts = prefs[KEY_FAILED_ATTEMPTS] ?: 0,
            lockOutUntilTimestamp = prefs[KEY_LOCKOUT_UNTIL] ?: 0L,
            intruderCaptureEnabled = prefs[KEY_INTRUDER_ENABLED] ?: false,
            intruderThresholdAttempts = prefs[KEY_INTRUDER_THRESHOLD] ?: 3,
            deviceAdminEnabled = prefs[KEY_DEVICE_ADMIN] ?: false,
            hideFromLauncher = prefs[KEY_HIDE_LAUNCHER] ?: false,
            customAppAlias = prefs[KEY_CUSTOM_APP_ALIAS] ?: "LockMe",
            customLogoUri = prefs[KEY_CUSTOM_LOGO_URI] ?: ""
        )
    }

    val protectedPackages: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()
    }

    val favoritePackages: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVORITE_PACKAGES] ?: emptySet()
    }

    suspend fun saveCredential(method: AuthMethod, salt: String, hash: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTH_METHOD] = method.name
            prefs[KEY_SALT] = salt
            prefs[KEY_HASH] = hash
            prefs[KEY_SETUP_COMPLETE] = true
            prefs[KEY_FAILED_ATTEMPTS] = 0
            prefs[KEY_LOCKOUT_UNTIL] = 0L
        }
    }

    suspend fun getCredentialVerifier(): Pair<String, String>? {
        var result: Pair<String, String>? = null
        context.dataStore.edit { prefs ->
            val salt = prefs[KEY_SALT]
            val hash = prefs[KEY_HASH]
            if (!salt.isNullOrBlank() && !hash.isNullOrBlank()) {
                result = Pair(salt, hash)
            }
        }
        return result
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setRelockPolicy(policy: RelockPolicy, customSeconds: Int = 0) {
        context.dataStore.edit { prefs ->
            prefs[KEY_RELOCK_POLICY] = policy.name
            if (policy == RelockPolicy.CUSTOM) {
                prefs[KEY_CUSTOM_RELOCK_SECONDS] = customSeconds
            }
        }
    }

    suspend fun setCustomRelockSeconds(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_RELOCK_SECONDS] = seconds
            prefs[KEY_RELOCK_POLICY] = RelockPolicy.CUSTOM.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setExpressivePalette(palette: ExpressivePalette) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EXPRESSIVE_PALETTE] = palette.name
            prefs[KEY_DYNAMIC_COLOR] = (palette == ExpressivePalette.DYNAMIC)
        }
    }

    suspend fun setLockscreenEffect(effect: LockscreenEffect) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOCKSCREEN_EFFECT] = effect.name
        }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_LANGUAGE] = language.name
        }
    }

    suspend fun setCompactMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_COMPACT_MODE] = enabled
        }
    }

    suspend fun setHaptics(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAPTICS] = enabled
        }
    }

    suspend fun setProtectionPaused(paused: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PROTECTION_PAUSED] = paused
        }
    }

    suspend fun setIntruderCaptureEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_INTRUDER_ENABLED] = enabled
        }
    }

    suspend fun setIntruderThreshold(threshold: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_INTRUDER_THRESHOLD] = threshold
        }
    }

    suspend fun setDeviceAdminEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEVICE_ADMIN] = enabled
        }
    }

    suspend fun setHideFromLauncher(hidden: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HIDE_LAUNCHER] = hidden
        }
    }

    suspend fun setCustomAppAlias(name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_APP_ALIAS] = name
        }
    }

    suspend fun setCustomLogoUri(uriString: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_LOGO_URI] = uriString
        }
    }

    suspend fun toggleFavorite(packageName: String) {
        context.dataStore.edit { prefs ->
            val set = (prefs[KEY_FAVORITE_PACKAGES] ?: emptySet()).toMutableSet()
            if (set.contains(packageName)) set.remove(packageName) else set.add(packageName)
            prefs[KEY_FAVORITE_PACKAGES] = set
        }
    }

    suspend fun recordFailedAttempt(lockoutUntil: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FAILED_ATTEMPTS] ?: 0
            prefs[KEY_FAILED_ATTEMPTS] = current + 1
            if (lockoutUntil > 0L) {
                prefs[KEY_LOCKOUT_UNTIL] = lockoutUntil
            }
        }
    }

    suspend fun resetFailedAttempts() {
        context.dataStore.edit { prefs ->
            prefs[KEY_FAILED_ATTEMPTS] = 0
            prefs[KEY_LOCKOUT_UNTIL] = 0L
        }
    }

    suspend fun toggleAppProtection(packageName: String, isProtected: Boolean) {
        context.dataStore.edit { prefs ->
            val currentSet = (prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()).toMutableSet()
            if (isProtected) {
                currentSet.add(packageName)
            } else {
                currentSet.remove(packageName)
            }
            prefs[KEY_PROTECTED_PACKAGES] = currentSet
        }
    }

    suspend fun setProtectedAppsBatch(packages: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PROTECTED_PACKAGES] = packages
        }
    }

    suspend fun resetAllSecurityData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
