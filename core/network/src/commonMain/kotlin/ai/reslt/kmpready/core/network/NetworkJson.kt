package ai.reslt.kmpready.core.network

import kotlinx.serialization.json.Json

/** Tolerant reader: new server fields never crash old app versions. */
val NetworkJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    encodeDefaults = true
    isLenient = false
}
