package com.example.ui.security

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.SecurityEventRepository
import com.example.data.security.CryptoHelper
import com.example.domain.model.AuthMethod
import com.example.domain.model.RelockPolicy
import com.example.domain.model.UserSecuritySettings
import com.example.platform.biometric.BiometricHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SecurityUiState(
    val settings: UserSecuritySettings = UserSecuritySettings(),
    val isBiometricHardwareAvailable: Boolean = false,
    val biometricStatusMessage: String = "",
    val showChangeCredDialog: Boolean = false,
    val isReauthenticating: Boolean = false
)

class SecurityViewModel(
    private val context: Context,
    private val dataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        checkBiometrics()
        viewModelScope.launch {
            dataStore.securitySettings.collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
    }

    private fun checkBiometrics() {
        val (available, message) = BiometricHelper.isBiometricAvailable(context)
        _uiState.value = _uiState.value.copy(
            isBiometricHardwareAvailable = available,
            biometricStatusMessage = message
        )
    }

    fun setBiometricsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setBiometricEnabled(enabled)
            SecurityEventRepository.logEvent(
                eventType = if (enabled) "Biometrics Enabled" else "Biometrics Disabled",
                details = "User updated biometric authentication preferences",
                isSuccess = true
            )
        }
    }

    fun setRelockPolicy(policy: RelockPolicy) {
        viewModelScope.launch {
            dataStore.setRelockPolicy(policy)
            SecurityEventRepository.logEvent(
                eventType = "Relock Policy Changed",
                details = "Changed policy to ${policy.displayName}",
                isSuccess = true
            )
        }
    }

    suspend fun verifyCurrentCredential(input: String): Boolean {
        val verifier = dataStore.getCredentialVerifier() ?: return false
        val isValid = CryptoHelper.verifyCredential(input, verifier.first, verifier.second)
        if (isValid) {
            SecurityEventRepository.logEvent(
                eventType = "Master Auth Success",
                details = "User authorized security settings access",
                isSuccess = true
            )
        } else {
            SecurityEventRepository.logEvent(
                eventType = "Master Auth Failed",
                details = "Invalid credential entered for security changes",
                isSuccess = false
            )
        }
        return isValid
    }

    suspend fun updateCredential(method: AuthMethod, newSecret: String) {
        val salt = CryptoHelper.generateSalt()
        val hash = CryptoHelper.hashCredential(newSecret, salt)
        dataStore.saveCredential(method, salt, hash)
        SecurityEventRepository.logEvent(
            eventType = "Credential Updated",
            details = "Changed security lock to $method",
            isSuccess = true
        )
    }

    class Factory(
        private val context: Context,
        private val dataStore: SettingsDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SecurityViewModel(context, dataStore) as T
        }
    }
}
