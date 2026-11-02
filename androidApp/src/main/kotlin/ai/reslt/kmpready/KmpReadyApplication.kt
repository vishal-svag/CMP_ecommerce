package ai.reslt.kmpready

import ai.reslt.kmpready.core.common.config.AppConfig
import ai.reslt.kmpready.di.initKoin
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class KmpReadyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            config = AppConfig(
                baseUrl = BuildConfig.API_BASE_URL,
                isDebug = BuildConfig.DEBUG,
                // Add SPKI pins before production, e.g.:
                // certificatePins = listOf(CertificatePin("api.reslt.ai", listOf("<primary>", "<backup>"))),
            ),
        ) {
            androidContext(this@KmpReadyApplication)
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.ERROR)
        }
    }
}
