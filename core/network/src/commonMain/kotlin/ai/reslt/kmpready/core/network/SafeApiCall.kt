package ai.reslt.kmpready.core.network

import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.result.AppResult
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * The only place where transport exceptions are allowed to exist. Everything above the
 * data layer receives an exhaustive [AppError]. Cancellation is always rethrown.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    AppResult.Failure(e.toAppError())
}

fun Throwable.toAppError(): AppError = when (this) {
    is ClientRequestException -> when (val code = response.status.value) {
        401 -> AppError.Unauthorized
        403 -> AppError.Forbidden
        404 -> AppError.NotFound
        429 -> AppError.TooManyRequests
        else -> AppError.Client(code)
    }
    is ServerResponseException -> AppError.Server(response.status.value)
    is RedirectResponseException -> AppError.Client(response.status.value)
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException,
    -> AppError.Timeout
    is JsonConvertException, is SerializationException -> AppError.Parsing
    is IOException -> AppError.Network
    else -> AppError.Unknown(this::class.simpleName)
}
