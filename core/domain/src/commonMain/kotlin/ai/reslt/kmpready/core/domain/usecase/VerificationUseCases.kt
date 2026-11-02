package ai.reslt.kmpready.core.domain.usecase

import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.error.AppError.Validation.Field
import ai.reslt.kmpready.core.common.error.AppError.Validation.Reason
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.domain.model.ExternalSignInMethod
import ai.reslt.kmpready.core.domain.model.LoginChallenge
import ai.reslt.kmpready.core.domain.model.User
import ai.reslt.kmpready.core.domain.model.VerificationFactor
import ai.reslt.kmpready.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class ObservePendingChallengeUseCase(private val repository: AuthRepository) {
    operator fun invoke(): StateFlow<LoginChallenge?> = repository.pendingChallenge
}

/** Step 2. Passcodes are validated locally (digits, exact length) before hitting the server. */
class VerifyLoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(factor: VerificationFactor): AppResult<User> {
        if (factor is VerificationFactor.Passcode) {
            val length = repository.pendingChallenge.value?.codeLength ?: DEFAULT_CODE_LENGTH
            val code = factor.code
            if (code.isEmpty()) return AppResult.Failure(AppError.Validation(Field.Code, Reason.Blank))
            if (code.length != length || !code.all(Char::isDigit)) {
                return AppResult.Failure(AppError.Validation(Field.Code, Reason.Invalid))
            }
        }
        return repository.verify(factor)
    }

    companion object {
        const val DEFAULT_CODE_LENGTH = 6
    }
}

class ResendCodeUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AppResult<LoginChallenge> = repository.resendCode()
}

class CancelLoginChallengeUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.cancelChallenge()
}

class ExternalSignInUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(method: ExternalSignInMethod): AppResult<User> = repository.signInWith(method)
}
