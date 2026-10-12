package com.example.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.AppRepository
import com.example.data.repository.SecurityEventRepository
import com.example.domain.model.AppProtection
import com.example.domain.model.PermissionStatus
import com.example.domain.model.SecurityEvent
import com.example.domain.model.UserSecuritySettings
import com.example.platform.monitoring.AppLockEngine
import com.example.platform.monitoring.LockMeForegroundService
import com.example.platform.permissions.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val settings: UserSecuritySettings = UserSecuritySettings(),
    val permissionStatus: PermissionStatus = PermissionStatus(false, false, false, false, false, false, 0L),
    val protectedAppsCount: Int = 0,
    val totalAppsCount: Int = 0,
    val recentEvents: List<SecurityEvent> = emptyList(),
    val isMonitoringActive: Boolean = false
)

class HomeViewModel(
    private val context: Context,
    private val dataStore: SettingsDataStore,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshState()
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            dataStore.securitySettings.collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
        viewModelScope.launch {
            dataStore.protectedPackages.collect { protectedPkgs ->
                _uiState.value = _uiState.value.copy(protectedAppsCount = protectedPkgs.size)
            }
        }
        viewModelScope.launch {
            SecurityEventRepository.events.collect { events ->
                _uiState.value = _uiState.value.copy(recentEvents = events.take(10))
            }
        }
    }

    fun refreshState() {
        val perm = PermissionManager.getFullPermissionStatus(context)
        val monitoring = AppLockEngine.isMonitoringActive()
        _uiState.value = _uiState.value.copy(
            permissionStatus = perm,
            isMonitoringActive = monitoring
        )
    }

    fun toggleProtection(enable: Boolean) {
        viewModelScope.launch {
            dataStore.setProtectionPaused(!enable)
            if (enable) {
                LockMeForegroundService.startService(context)
                AppLockEngine.startMonitoring(context, dataStore)
                SecurityEventRepository.logEvent(
                    eventType = "Protection Resumed",
                    details = "User enabled app protection from dashboard",
                    isSuccess = true
                )
            } else {
                SecurityEventRepository.logEvent(
                    eventType = "Protection Paused",
                    details = "User paused app protection from dashboard",
                    isSuccess = false
                )
            }
            refreshState()
        }
    }

    fun clearEvents() {
        SecurityEventRepository.clearEvents()
    }

    class Factory(
        private val context: Context,
        private val dataStore: SettingsDataStore,
        private val appRepository: AppRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(context, dataStore, appRepository) as T
        }
    }
}
