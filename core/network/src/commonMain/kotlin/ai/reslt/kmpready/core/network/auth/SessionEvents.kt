package ai.reslt.kmpready.core.network.auth

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface SessionEvent {
    /** The refresh token was rejected; tokens have already been wiped. */
    data object Expired : SessionEvent
}

/**
 * One-way signal from the network layer to whoever owns the session (AuthRepository).
 * Backed by a buffered Channel so the event is never lost if nobody is collecting yet.
 */
class SessionEvents {
    private val channel = Channel<SessionEvent>(Channel.BUFFERED)
    val events: Flow<SessionEvent> = channel.receiveAsFlow()

    internal fun emit(event: SessionEvent) {
        channel.trySend(event)
    }
}
