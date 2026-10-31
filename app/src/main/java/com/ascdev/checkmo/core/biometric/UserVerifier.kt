package com.ascdev.checkmo.core.biometric

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed interface VerificationResult {
    data object Success : VerificationResult
    data object Cancelled : VerificationResult

    /** The device has no fingerprint, face or screen lock set up. */
    data object Unavailable : VerificationResult
    data class Error(val message: String) : VerificationResult
}

/**
 * Confirms the person holding the phone is its owner before recording a time entry.
 *
 * Uses biometrics with the screen lock (PIN, pattern or password) as fallback. BIOMETRIC_WEAK is
 * used because BIOMETRIC_STRONG combined with DEVICE_CREDENTIAL is not supported on API 28-29.
 */
class UserVerifier(private val activity: FragmentActivity) {

    suspend fun verify(title: String, subtitle: String): VerificationResult {
        val canAuthenticate = BiometricManager.from(activity).canAuthenticate(Authenticators)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) return VerificationResult.Unavailable

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(Authenticators)
            .build()

        return suspendCancellableCoroutine { continuation ->
            val prompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (continuation.isActive) continuation.resume(VerificationResult.Success)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        val result = if (errorCode in CancelErrors) {
                            VerificationResult.Cancelled
                        } else {
                            VerificationResult.Error(errString.toString())
                        }
                        if (continuation.isActive) continuation.resume(result)
                    }

                    // A single unrecognized finger keeps the prompt open, so there is nothing to resume.
                    override fun onAuthenticationFailed() = Unit
                },
            )
            prompt.authenticate(promptInfo)
            continuation.invokeOnCancellation { prompt.cancelAuthentication() }
        }
    }

    private companion object {
        const val Authenticators = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

        val CancelErrors = setOf(
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            BiometricPrompt.ERROR_CANCELED,
        )
    }
}

@Composable
fun rememberUserVerifier(): UserVerifier {
    val activity = checkNotNull(LocalActivity.current as? FragmentActivity) {
        "UserVerifier must be used inside a FragmentActivity"
    }
    return remember(activity) { UserVerifier(activity) }
}
