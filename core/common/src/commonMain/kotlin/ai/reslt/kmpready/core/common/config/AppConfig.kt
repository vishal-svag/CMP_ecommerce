package ai.reslt.kmpready.core.common.config

/**
 * Build-time environment, supplied by each platform entry point
 * (Android BuildConfig / iOS xcconfig). Never put secrets here — they ship inside the binary.
 */
data class AppConfig(
    val baseUrl: String,
    val isDebug: Boolean,
    val certificatePins: List<CertificatePin> = emptyList(),
    /** Concierge contact (mailto:/tel:/https:). Null hides the actions. */
    val conciergeEmailUri: String? = "mailto:concierge@reslt.ai",
    val conciergePhoneUri: String? = null,
) {
    init {
        require(baseUrl.endsWith("/")) { "baseUrl must end with '/'" }
        require(isDebug || baseUrl.startsWith("https://")) { "Release builds must use HTTPS" }
    }
}

/**
 * SHA-256 SPKI pins for a host, base64-encoded (without the "sha256/" prefix).
 * Always ship at least two pins (current + backup key) or a key rotation will brick the app.
 */
data class CertificatePin(
    val host: String,
    val sha256: List<String>,
)
