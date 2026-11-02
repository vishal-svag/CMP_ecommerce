package ai.reslt.kmpready.core.domain.model

enum class IdentifierType {
    /** Email address or Atelier client ID. */
    ClientIdOrEmail,

    /** Numeric VIP member number. */
    MemberNumber,
}

/** toString is overridden so a password can never leak into logs or crash reports. */
data class Credentials(
    val identifier: String,
    val password: String,
    val identifierType: IdentifierType = IdentifierType.ClientIdOrEmail,
    /** Persist the session (encrypted) for [REMEMBER_ME_DAYS]; otherwise it lives in memory only. */
    val rememberMe: Boolean = false,
) {
    override fun toString(): String =
        "Credentials(identifier=$identifier, type=$identifierType, rememberMe=$rememberMe, password=***)"

    companion object {
        const val REMEMBER_ME_DAYS = 30
    }
}
