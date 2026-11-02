package ai.reslt.kmpready.app

import ai.reslt.kmpready.core.domain.model.SessionState
import ai.reslt.kmpready.core.domain.usecase.ObserveSessionUseCase
import ai.reslt.kmpready.core.domain.usecase.RestoreSessionUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    observeSession: ObserveSessionUseCase,
    private val restoreSession: RestoreSessionUseCase,
) : ViewModel() {

    val session: StateFlow<SessionState> = observeSession()

    init {
        viewModelScope.launch { restoreSession() }
    }
}
