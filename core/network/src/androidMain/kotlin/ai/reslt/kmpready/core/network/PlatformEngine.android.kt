package ai.reslt.kmpready.core.network

import ai.reslt.kmpready.core.common.config.CertificatePin
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.CertificatePinner

internal actual fun createPlatformEngine(pins: List<CertificatePin>): HttpClientEngine = OkHttp.create {
    config {
        retryOnConnectionFailure(true)
        if (pins.isNotEmpty()) {
            val pinner = CertificatePinner.Builder().apply {
                pins.forEach { pin -> pin.sha256.forEach { hash -> add(pin.host, "sha256/$hash") } }
            }.build()
            certificatePinner(pinner)
        }
    }
}
