package ai.reslt.kmpready.core.common.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

/** Single place that decides how verbose logging is. Release builds only log warnings and above. */
object AppLogger {
    fun configure(isDebug: Boolean) {
        Logger.setMinSeverity(if (isDebug) Severity.Verbose else Severity.Warn)
        Logger.setTag("KmpReady")
    }

    fun withTag(tag: String): Logger = Logger.withTag(tag)
}
