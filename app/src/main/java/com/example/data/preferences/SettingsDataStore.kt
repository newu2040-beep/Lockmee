package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AuthMethod
import com.example.domain.model.ExpressivePalette
import com.example.domain.model.RelockPolicy
import com.example.domain.model.ThemeMode
import com.example.domain.model.UserSecuritySettings
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
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_EXPRESSIVE_PALETTE = stringPreferencesKey("expressive_palette")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_COMPACT_MODE = booleanPreferencesKey("compact_mode")
        private val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")
        private val KEY_PROTECTION_PAUSED = booleanPreferencesKey("protection_paused")
        private val KEY_FAILED_ATTEMPTS = intPreferencesKey("failed_attempts")
        private val KEY_LOCKOUT_UNTIL = longPreferencesKey("lockout_until")
        private val KEY_PROTECTED_PACKAGES = stringSetPreferencesKey("protected_packages")
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
            dynamicColorEnabled = prefs[KEY_DYNAMIC_COLOR] ?: true,
            compactModeEnabled = prefs[KEY_COMPACT_MODE] ?: false,
            hapticsEnabled = prefs[KEY_HAPTICS] ?: true,
            protectionPaused = prefs[KEY_PROTECTION_PAUSED] ?: false,
            failedAttempts = prefs[KEY_FAILED_ATTEMPTS] ?: 0,
            lockOutUntilTimestamp = prefs[KEY_LOCKOUT_UNTIL] ?: 0L
        )
    }

    val protectedPackages: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_PROTECTED_PACKAGES] ?: emptySet()
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

    suspend fun setRelockPolicy(policy: RelockPolicy) {
        context.dataStore.edit { prefs ->
            prefs[KEY_RELOCK_POLICY] = policy.name
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

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DYNAMIC_COLOR] = enabled
            if (enabled) {
                prefs[KEY_EXPRESSIVE_PALETTE] = ExpressivePalette.DYNAMIC.name
            }
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
