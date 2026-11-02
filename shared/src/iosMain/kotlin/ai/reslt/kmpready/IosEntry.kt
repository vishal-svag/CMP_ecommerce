package ai.reslt.kmpready

import ai.reslt.kmpready.core.common.config.AppConfig
import ai.reslt.kmpready.di.initKoin

/**
 * Called once from `iOSApp.init()`:
 *   IosEntryKt.startKoinIos(baseUrl: ..., isDebug: ...)
 */
@Suppress("unused")
fun startKoinIos(baseUrl: String, isDebug: Boolean) {
    initKoin(AppConfig(baseUrl = baseUrl, isDebug = isDebug))
}
