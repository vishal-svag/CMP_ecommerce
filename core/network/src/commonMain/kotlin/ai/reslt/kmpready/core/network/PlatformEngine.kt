package ai.reslt.kmpready.core.network

import ai.reslt.kmpready.core.common.config.CertificatePin
import io.ktor.client.engine.HttpClientEngine

/** OkHttp on Android, NSURLSession (Darwin) on iOS — both with certificate pinning support. */
internal expect fun createPlatformEngine(pins: List<CertificatePin>): HttpClientEngine
