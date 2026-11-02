package ai.reslt.kmpready.core.common.biometric

/**
 * Platform biometric prompt (Android BiometricPrompt / iOS LocalAuthentication).
 * Lives in :core:common (not :core:domain) because it is a device capability, not business
 * logic — and so :core:security can implement it without depending on the domain layer.
 *
 * A local success only proves presence on THIS device; the server must still verify a
 * device-bound key before treating it as an authentication factor.
 */
interface BiometricAuthenticator {
    fun availability(): BiometricAvailability

    suspend fun authenticate(request: BiometricRequest): BiometricResult
}

/** Text shown in the system prompt. Named "Request" to avoid clashing with androidx.biometric.BiometricPrompt. */
data class BiometricRequest(
    val title: String,
    val subtitle: String,
    /** Android negative button / iOS cancel title. */
    val cancelLabel: String,
)

sealed interface BiometricAvailability {
    data class Available(val kind: BiometricKind) : BiometricAvailability
    data object NotEnrolled : BiometricAvailability
    data object Unsupported : BiometricAvailability
}

enum class BiometricKind { Face, Fingerprint, Generic }

sealed interface BiometricResult {
    data object Success : BiometricResult
    data object Cancelled : BiometricResult
    data object Unavailable : BiometricResult
    data class Failed(val reason: String?) : BiometricResult
}
