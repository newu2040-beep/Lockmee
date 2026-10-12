package com.example.platform.biometric

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    fun isBiometricAvailable(context: Context): Pair<Boolean, String> {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Pair(true, "Native Biometric (Fingerprint / Face Unlock) enrolled & active")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Pair(false, "No biometric / face sensor detected on this device")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Pair(false, "Biometric hardware is busy or temporarily unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Pair(false, "No biometric or face credentials enrolled in Android Settings")
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> Pair(false, "Device requires a system security update for biometrics")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> Pair(false, "Biometric standard unsupported")
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> Pair(false, "Biometric sensor status unknown")
            else -> Pair(false, "Biometric hardware unavailable")
        }
    }

    fun promptBiometric(
        activity: FragmentActivity,
        title: String = "Unlock App",
        subtitle: String = "Verify your identity with fingerprint or face lock",
        negativeButtonText: String = "Use PIN / Pattern",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription("Place your finger on the sensor or look at your camera to unlock")
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .setConfirmationRequired(false)
            .build()

        prompt.authenticate(promptInfo)
    }
}
