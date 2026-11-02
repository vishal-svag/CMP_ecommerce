package ai.reslt.kmpready.core.domain.model

sealed interface SessionState {
    /** Startup: secure storage hasn't been read yet. Show a splash, not the login screen. */
    data object Unknown : SessionState
    data object LoggedIn : SessionState

    /** Browsing without an account: public catalogue only, no tokens, not persisted across launches. */
    data object Guest : SessionState
    data class LoggedOut(val reason: Reason = Reason.None) : SessionState {
        enum class Reason { None, UserInitiated, Expired }
    }
}
