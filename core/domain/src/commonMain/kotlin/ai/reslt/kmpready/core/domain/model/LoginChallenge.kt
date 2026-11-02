package ai.reslt.kmpready.core.domain.model

/**
 * Step 2 of sign-in. Issued after the password is accepted; the session is NOT active until
 * the challenge is satisfied. Contains display data only — never tokens.
 */
data class LoginChallenge(
    val id: String,
    val displayName: String,
    /** Public member reference, e.g. "ATL-84920". */
    val memberReference: String,
    val codeLength: Int,
    val expiresAtMillis: Long,
    val resendAvailableAtMillis: Long,
)

sealed interface VerificationFactor {
    data class Passcode(val code: String) : VerificationFactor {
        override fun toString(): String = "Passcode(***)"
    }

    /** User passed the on-device biometric prompt (bound to a device key server-side in production). */
    data object DeviceBiometric : VerificationFactor
}

enum class ExternalSignInMethod { Apple, Passkey }
