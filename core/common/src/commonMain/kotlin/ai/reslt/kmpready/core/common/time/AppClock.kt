package ai.reslt.kmpready.core.common.time

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Wall-clock abstraction so expiry / countdown logic is testable. */
fun interface AppClock {
    fun nowMillis(): Long
}

@OptIn(ExperimentalTime::class)
object SystemAppClock : AppClock {
    override fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
