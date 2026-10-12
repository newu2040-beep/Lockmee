package com.example.ui.apps

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.AppRepository
import com.example.data.repository.SecurityEventRepository
import com.example.domain.model.AppCategory
import com.example.domain.model.AppProtection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppsUiState(
    val isLoading: Boolean = true,
    val allApps: List<AppProtection> = emptyList(),
    val filteredApps: List<AppProtection> = emptyList(),
    val searchQuery: String = "",
    val activeCategory: AppCategory = AppCategory.ALL,
    val protectedCount: Int = 0,
    val gamesCount: Int = 0,
    val regularAppsCount: Int = 0,
    val selectedAppForDetails: AppProtection? = null
)

class AppsViewModel(
    private val appRepository: AppRepository,
    private val dataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            appRepository.installedAppsFlow.collect { apps ->
                val protectedCount = apps.count { it.isProtected }
                val gamesCount = apps.count { it.isGame }
                val regularAppsCount = apps.size - gamesCount
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allApps = apps,
                    protectedCount = protectedCount,
                    gamesCount = gamesCount,
                    regularAppsCount = regularAppsCount,
                    filteredApps = applyFilterAndSearch(apps, _uiState.value.searchQuery, _uiState.value.activeCategory)
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            searchQuery = query,
            filteredApps = applyFilterAndSearch(currentState.allApps, query, currentState.activeCategory)
        )
    }

    fun onCategoryChanged(category: AppCategory) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            activeCategory = category,
            filteredApps = applyFilterAndSearch(currentState.allApps, currentState.searchQuery, category)
        )
    }

    fun toggleAppProtection(packageName: String, isProtected: Boolean) {
        viewModelScope.launch {
            appRepository.toggleProtection(packageName, isProtected)
            SecurityEventRepository.logEvent(
                eventType = if (isProtected) "App Protected" else "Protection Removed",
                details = "$packageName protection set to $isProtected",
                isSuccess = true
            )
        }
    }

    fun protectAllFiltered() {
        viewModelScope.launch {
            val toProtect = _uiState.value.filteredApps.map { it.packageName }.toSet()
            val currentProtected = _uiState.value.allApps.filter { it.isProtected }.map { it.packageName }.toSet()
            appRepository.setProtectedAppsBatch(currentProtected + toProtect)
            SecurityEventRepository.logEvent(
                eventType = "Batch Protect",
                details = "Protected ${toProtect.size} apps/games",
                isSuccess = true
            )
        }
    }

    fun unprotectAllFiltered() {
        viewModelScope.launch {
            val toUnprotect = _uiState.value.filteredApps.map { it.packageName }.toSet()
            val currentProtected = _uiState.value.allApps.filter { it.isProtected }.map { it.packageName }.toSet()
            appRepository.setProtectedAppsBatch(currentProtected - toUnprotect)
            SecurityEventRepository.logEvent(
                eventType = "Batch Unprotect",
                details = "Unprotected ${toUnprotect.size} apps/games",
                isSuccess = true
            )
        }
    }

    fun selectAppForDetails(app: AppProtection?) {
        _uiState.value = _uiState.value.copy(selectedAppForDetails = app)
    }

    private fun applyFilterAndSearch(
        apps: List<AppProtection>,
        query: String,
        category: AppCategory
    ): List<AppProtection> {
        return apps.filter { app ->
            val matchesCategory = when (category) {
                AppCategory.ALL -> true
                AppCategory.GAMES -> app.isGame
                AppCategory.APPS -> !app.isGame
                AppCategory.PROTECTED -> app.isProtected
                AppCategory.UNPROTECTED -> !app.isProtected
            }
            val matchesSearch = query.isBlank() ||
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    class Factory(
        private val appRepository: AppRepository,
        private val dataStore: SettingsDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppsViewModel(appRepository, dataStore) as T
        }
    }
}
