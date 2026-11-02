package ai.reslt.kmpready.core.security

import ai.reslt.kmpready.core.common.biometric.BiometricAuthenticator
import ai.reslt.kmpready.core.common.biometric.BiometricAvailability
import ai.reslt.kmpready.core.common.biometric.BiometricKind
import ai.reslt.kmpready.core.common.biometric.BiometricRequest
import ai.reslt.kmpready.core.common.biometric.BiometricResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.LocalAuthentication.LABiometryTypeFaceID
import platform.LocalAuthentication.LABiometryTypeTouchID
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlin.coroutines.resume

/** LocalAuthentication (Face ID / Touch ID). Requires NSFaceIDUsageDescription in Info.plist. */
@OptIn(ExperimentalForeignApi::class)
internal class IosBiometricAuthenticator : BiometricAuthenticator {

    override fun availability(): BiometricAvailability = memScoped {
        val context = LAContext()
        val error = alloc<ObjCObjectVar<NSError?>>()
        val canEvaluate = context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, error.ptr)
        when {
            canEvaluate -> BiometricAvailability.Available(
                when (context.biometryType) {
                    LABiometryTypeFaceID -> BiometricKind.Face
                    LABiometryTypeTouchID -> BiometricKind.Fingerprint
                    else -> BiometricKind.Generic
                },
            )
            error.value?.code == LA_ERROR_BIOMETRY_NOT_ENROLLED -> BiometricAvailability.NotEnrolled
            else -> BiometricAvailability.Unsupported
        }
    }

    override suspend fun authenticate(request: BiometricRequest): BiometricResult {
        if (availability() !is BiometricAvailability.Available) return BiometricResult.Unavailable
        val context = LAContext().apply { localizedCancelTitle = request.cancelLabel }
        return suspendCancellableCoroutine<BiometricResult> { continuation ->
            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = request.subtitle,
            ) { success, error ->
                if (!continuation.isActive) return@evaluatePolicy
                val result: BiometricResult = when {
                    success -> BiometricResult.Success
                    error?.code in CANCEL_CODES -> BiometricResult.Cancelled
                    else -> BiometricResult.Failed(error?.localizedDescription)
                }
                continuation.resume(result)
            }
            continuation.invokeOnCancellation { context.invalidate() }
        }
    }

    private companion object {
        // LAError codes (LAError.h): userCancel = -2, userFallback = -3, systemCancel = -4,
        // appCancel = -9, biometryNotEnrolled = -7.
        val CANCEL_CODES = setOf(-2L, -3L, -4L, -9L)
        const val LA_ERROR_BIOMETRY_NOT_ENROLLED = -7L
    }
}
