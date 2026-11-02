package ai.reslt.kmpready.core.network

import ai.reslt.kmpready.core.common.config.CertificatePin
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.certificates.CertificatePinner

internal actual fun createPlatformEngine(pins: List<CertificatePin>): HttpClientEngine = Darwin.create {
    if (pins.isNotEmpty()) {
        val pinner = CertificatePinner.Builder().apply {
            pins.forEach { pin -> pin.sha256.forEach { hash -> add(pin.host, "sha256/$hash") } }
        }.build()
        handleChallenge(pinner)
    }
}
