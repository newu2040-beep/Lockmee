package com.example.ui.settings

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.SettingsDataStore
import com.example.domain.model.*
import com.example.platform.monitoring.AppLockEngine
import com.example.platform.permissions.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSecuritySettings = UserSecuritySettings(),
    val permissionStatus: PermissionStatus = PermissionStatus(false, false, false, false, false, false, 0L),
    val isMonitoringActive: Boolean = false
)

class SettingsViewModel(
    private val context: Context,
    private val dataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshStatus()
        viewModelScope.launch {
            dataStore.securitySettings.collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
    }

    fun refreshStatus() {
        val perm = PermissionManager.getFullPermissionStatus(context)
        val monitoring = AppLockEngine.isMonitoringActive()
        _uiState.value = _uiState.value.copy(
            permissionStatus = perm,
            isMonitoringActive = monitoring
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            dataStore.setThemeMode(mode)
        }
    }

    fun setExpressivePalette(palette: ExpressivePalette) {
        viewModelScope.launch {
            dataStore.setExpressivePalette(palette)
        }
    }

    fun setLockscreenEffect(effect: LockscreenEffect) {
        viewModelScope.launch {
            dataStore.setLockscreenEffect(effect)
        }
    }

    fun setCompactMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setCompactMode(enabled)
        }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setHaptics(enabled)
        }
    }

    fun setCustomAppAlias(name: String) {
        viewModelScope.launch {
            dataStore.setCustomAppAlias(name)
        }
    }

    fun setCustomLogoUri(uriString: String) {
        viewModelScope.launch {
            dataStore.setCustomLogoUri(uriString)
        }
    }

    fun toggleDeviceAdmin(activity: Activity, enable: Boolean) {
        viewModelScope.launch {
            if (enable) {
                PermissionManager.requestDeviceAdmin(activity)
            } else {
                PermissionManager.removeDeviceAdmin(context)
            }
            dataStore.setDeviceAdminEnabled(enable)
            refreshStatus()
        }
    }

    fun toggleHideFromLauncher(hide: Boolean) {
        viewModelScope.launch {
            PermissionManager.setAppHiddenFromLauncher(context, hide)
            dataStore.setHideFromLauncher(hide)
            refreshStatus()
        }
    }

    class Factory(
        private val context: Context,
        private val dataStore: SettingsDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(context, dataStore) as T
        }
    }
}
