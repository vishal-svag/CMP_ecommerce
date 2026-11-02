package ai.reslt.kmpready.core.ui

import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.ui.resources.Res
import ai.reslt.kmpready.core.ui.resources.error_code_expired
import ai.reslt.kmpready.core.ui.resources.error_field_invalid
import ai.reslt.kmpready.core.ui.resources.error_field_required
import ai.reslt.kmpready.core.ui.resources.error_field_too_short
import ai.reslt.kmpready.core.ui.resources.error_forbidden
import ai.reslt.kmpready.core.ui.resources.error_invalid_code
import ai.reslt.kmpready.core.ui.resources.error_invalid_credentials
import ai.reslt.kmpready.core.ui.resources.error_network
import ai.reslt.kmpready.core.ui.resources.error_no_pending_verification
import ai.reslt.kmpready.core.ui.resources.error_not_found
import ai.reslt.kmpready.core.ui.resources.error_parsing
import ai.reslt.kmpready.core.ui.resources.error_server
import ai.reslt.kmpready.core.ui.resources.error_session_expired
import ai.reslt.kmpready.core.ui.resources.error_storage
import ai.reslt.kmpready.core.ui.resources.error_timeout
import ai.reslt.kmpready.core.ui.resources.error_too_many_requests
import ai.reslt.kmpready.core.ui.resources.error_unavailable
import ai.reslt.kmpready.core.ui.resources.error_unknown
import ai.reslt.kmpready.core.ui.resources.error_verification_locked
import ai.reslt.kmpready.core.ui.resources.retry

/** Single, exhaustive AppError -> user message mapping. Adding an AppError forces an update here. */
fun AppError.toUiText(): UiText = when (this) {
    AppError.Network -> UiText.Resource(Res.string.error_network)
    AppError.Timeout -> UiText.Resource(Res.string.error_timeout)
    AppError.Unauthorized -> UiText.Resource(Res.string.error_session_expired)
    AppError.Forbidden -> UiText.Resource(Res.string.error_forbidden)
    AppError.NotFound -> UiText.Resource(Res.string.error_not_found)
    AppError.TooManyRequests -> UiText.Resource(Res.string.error_too_many_requests)
    AppError.InvalidCredentials -> UiText.Resource(Res.string.error_invalid_credentials)
    is AppError.InvalidCode -> UiText.Resource(Res.string.error_invalid_code, listOf(attemptsRemaining))
    AppError.CodeExpired -> UiText.Resource(Res.string.error_code_expired)
    AppError.VerificationLocked -> UiText.Resource(Res.string.error_verification_locked)
    AppError.NoPendingVerification -> UiText.Resource(Res.string.error_no_pending_verification)
    AppError.Unavailable -> UiText.Resource(Res.string.error_unavailable)
    is AppError.Client -> UiText.Resource(Res.string.error_unknown)
    is AppError.Server -> UiText.Resource(Res.string.error_server)
    AppError.Parsing -> UiText.Resource(Res.string.error_parsing)
    AppError.Storage -> UiText.Resource(Res.string.error_storage)
    is AppError.Validation -> UiText.Resource(
        when (reason) {
            AppError.Validation.Reason.Blank -> Res.string.error_field_required
            AppError.Validation.Reason.TooShort -> Res.string.error_field_too_short
            AppError.Validation.Reason.Invalid -> Res.string.error_field_invalid
        },
    )
    is AppError.Unknown -> UiText.Resource(Res.string.error_unknown)
}

object CommonText {
    val retry: UiText get() = UiText.Resource(Res.string.retry)
    val sessionExpired: UiText get() = UiText.Resource(Res.string.error_session_expired)
}
