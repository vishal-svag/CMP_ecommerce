package ai.reslt.kmpready.feature.auth.components

import ai.reslt.kmpready.core.designsystem.component.AppIconButton
import ai.reslt.kmpready.core.designsystem.component.AppIcons
import ai.reslt.kmpready.core.designsystem.component.BrandWordmark
import ai.reslt.kmpready.core.designsystem.theme.AppTheme
import ai.reslt.kmpready.feature.auth.resources.Res
import ai.reslt.kmpready.feature.auth.resources.brand_subtitle
import ai.reslt.kmpready.feature.auth.resources.brand_title
import ai.reslt.kmpready.feature.auth.resources.welcome_close
import ai.reslt.kmpready.feature.auth.resources.welcome_help
import ai.reslt.kmpready.feature.auth.resources.welcome_help_body
import ai.reslt.kmpready.feature.auth.resources.welcome_help_dismiss
import ai.reslt.kmpready.feature.auth.resources.welcome_help_title
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource

/** ✕ — ATELIER wordmark — ? header shared by every auth screen. */
@Composable
internal fun AuthHeader(onClose: () -> Unit, onHelp: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(top = AppTheme.spacing.xs)) {
        AppIconButton(
            icon = AppIcons.Close,
            contentDescription = stringResource(Res.string.welcome_close),
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        BrandWordmark(
            title = stringResource(Res.string.brand_title),
            subtitle = stringResource(Res.string.brand_subtitle),
            modifier = Modifier.align(Alignment.Center),
        )
        AppIconButton(
            icon = AppIcons.Help,
            contentDescription = stringResource(Res.string.welcome_help),
            onClick = onHelp,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Composable
internal fun ConciergeHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.welcome_help_title), style = MaterialTheme.typography.titleLarge) },
        text = { Text(stringResource(Res.string.welcome_help_body), style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.welcome_help_dismiss).uppercase(), style = MaterialTheme.typography.labelLarge)
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = MaterialTheme.shapes.large,
    )
}
