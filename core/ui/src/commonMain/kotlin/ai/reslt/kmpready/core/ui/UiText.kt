package ai.reslt.kmpready.core.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Text that ViewModels can produce without a Compose/Context dependency.
 * Resolved to a localized String only at render time.
 */
sealed interface UiText {
    data class Dynamic(val value: String) : UiText
    data class Resource(val resource: StringResource, val args: List<Any> = emptyList()) : UiText
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> stringResource(resource, *args.toTypedArray())
}

/** Non-composable resolution, e.g. for a snackbar shown from an effect collector. */
suspend fun UiText.resolve(): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> getString(resource, *args.toTypedArray())
}
