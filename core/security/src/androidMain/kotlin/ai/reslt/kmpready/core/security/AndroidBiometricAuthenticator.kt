package ai.reslt.kmpready.core.security

import ai.reslt.kmpready.core.common.biometric.BiometricAuthenticator
import ai.reslt.kmpready.core.common.biometric.BiometricAvailability
import ai.reslt.kmpready.core.common.biometric.BiometricKind
import ai.reslt.kmpready.core.common.biometric.BiometricRequest
import ai.reslt.kmpready.core.common.biometric.BiometricResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/** Class 3 (BIOMETRIC_STRONG) only — the level required to unlock Keystore-bound keys. */
internal class AndroidBiometricAuthenticator(
    private val context: Context,
    private val activityTracker: CurrentActivityTracker,
) : BiometricAuthenticator {

    override fun availability(): BiometricAvailability =
        when (BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available(kind())
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NotEnrolled
            else -> BiometricAvailability.Unsupported
        }

    override suspend fun authenticate(request: BiometricRequest): BiometricResult = withContext<BiometricResult>(Dispatchers.Main) {
        val activity = activityTracker.fragmentActivity ?: return@withContext BiometricResult.Unavailable
        if (availability() !is BiometricAvailability.Available) return@withContext BiometricResult.Unavailable

        // Explicit type: otherwise T is inferred from the early returns above (too narrow) and resume() won't compile.
        suspendCancellableCoroutine<BiometricResult> { continuation ->
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (continuation.isActive) continuation.resume(BiometricResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (!continuation.isActive) return
                    val result: BiometricResult = when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED,
                        -> BiometricResult.Cancelled
                        BiometricPrompt.ERROR_HW_NOT_PRESENT,
                        BiometricPrompt.ERROR_HW_UNAVAILABLE,
                        BiometricPrompt.ERROR_NO_BIOMETRICS,
                        -> BiometricResult.Unavailable
                        else -> BiometricResult.Failed(errString.toString())
                    }
                    continuation.resume(result)
                }
                // onAuthenticationFailed = single non-match; the system prompt lets the user retry.
            }

            val biometricPrompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
            biometricPrompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(request.title)
                    .setSubtitle(request.subtitle)
                    .setNegativeButtonText(request.cancelLabel)
                    .setAllowedAuthenticators(BIOMETRIC_STRONG)
                    .setConfirmationRequired(false)
                    .build(),
            )
            continuation.invokeOnCancellation { biometricPrompt.cancelAuthentication() }
        }
    }

    private fun kind(): BiometricKind {
        val pm = context.packageManager
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && pm.hasSystemFeature(PackageManager.FEATURE_FACE) &&
                !pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) -> BiometricKind.Face
            pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) -> BiometricKind.Fingerprint
            else -> BiometricKind.Generic
        }
    }
}
