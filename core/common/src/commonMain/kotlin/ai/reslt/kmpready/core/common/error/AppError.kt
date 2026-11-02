package ai.reslt.kmpready.core.common.error

/**
 * Every failure the app can surface. Data-layer exceptions are mapped into this hierarchy
 * at the boundary, so nothing above :core:data ever sees a Ktor / platform exception.
 */
sealed interface AppError {

    /** No connectivity or the connection dropped. */
    data object Network : AppError

    data object Timeout : AppError

    /** Session is missing/expired and could not be refreshed. */
    data object Unauthorized : AppError

    data object Forbidden : AppError

    data object NotFound : AppError

    data object TooManyRequests : AppError

    /** Login specifically rejected the identifier/password pair. */
    data object InvalidCredentials : AppError

    /** Second factor: wrong passcode. */
    data class InvalidCode(val attemptsRemaining: Int) : AppError

    /** Second factor: passcode expired — request a new one. */
    data object CodeExpired : AppError

    /** Second factor: attempt limit reached — the sign-in must restart. */
    data object VerificationLocked : AppError

    /** No pending sign-in (e.g. process was killed between steps). */
    data object NoPendingVerification : AppError

    /** Capability not available yet / on this device (e.g. Sign in with Apple before backend support). */
    data object Unavailable : AppError

    data class Client(val code: Int) : AppError

    data class Server(val code: Int) : AppError

    /** Response could not be parsed — usually an API contract mismatch. */
    data object Parsing : AppError

    /** Client-side input validation failed before any request was made. */
    data class Validation(val field: Field, val reason: Reason) : AppError {
        enum class Field { Identifier, Password, Code }
        enum class Reason { Blank, TooShort, Invalid }
    }

    /** Secure storage (Keystore / Keychain) failed. */
    data object Storage : AppError

    data class Unknown(val message: String? = null) : AppError
}
