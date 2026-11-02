package ai.reslt.kmpready.core.domain.usecase

import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.domain.model.SessionState
import ai.reslt.kmpready.core.domain.model.User
import ai.reslt.kmpready.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveSessionUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): StateFlow<SessionState> = authRepository.session
}

class RestoreSessionUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() = authRepository.restoreSession()
}

class ContinueAsGuestUseCase(private val authRepository: AuthRepository) {
    operator fun invoke() = authRepository.continueAsGuest()
}

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() = authRepository.logout()
}

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): AppResult<User> = authRepository.currentUser()
}
