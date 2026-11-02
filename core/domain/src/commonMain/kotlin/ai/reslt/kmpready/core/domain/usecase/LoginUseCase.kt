package ai.reslt.kmpready.core.domain.usecase

import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.error.AppError.Validation.Field
import ai.reslt.kmpready.core.common.error.AppError.Validation.Reason
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.domain.model.Credentials
import ai.reslt.kmpready.core.domain.model.IdentifierType
import ai.reslt.kmpready.core.domain.model.LoginChallenge
import ai.reslt.kmpready.core.domain.repository.AuthRepository

/** Step 1: validates input, then exchanges credentials for a second-factor challenge. */
class LoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(credentials: Credentials): AppResult<LoginChallenge> {
        val normalized = credentials.copy(identifier = credentials.identifier.trim())
        validate(normalized)?.let { return AppResult.Failure(it) }
        return authRepository.login(normalized)
    }

    /** Exposed so the UI can validate inline without triggering a request. */
    fun validate(credentials: Credentials): AppError.Validation? {
        val id = credentials.identifier.trim()
        return when {
            id.isBlank() -> AppError.Validation(Field.Identifier, Reason.Blank)
            credentials.identifierType == IdentifierType.MemberNumber && !MEMBER_NUMBER.matches(id) ->
                AppError.Validation(Field.Identifier, Reason.Invalid)
            credentials.password.isBlank() -> AppError.Validation(Field.Password, Reason.Blank)
            credentials.password.length < MIN_PASSWORD_LENGTH -> AppError.Validation(Field.Password, Reason.TooShort)
            else -> null
        }
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
        private val MEMBER_NUMBER = Regex("^[0-9]{4,12}$")
    }
}
