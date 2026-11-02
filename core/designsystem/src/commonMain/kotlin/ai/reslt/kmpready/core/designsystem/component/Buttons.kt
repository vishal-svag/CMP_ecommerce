package ai.reslt.kmpready.core.designsystem.component

import ai.reslt.kmpready.core.designsystem.theme.AppTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource

private val ButtonMinHeight = 54.dp
private val ButtonPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)

/**
 * Full-width ink CTA with a tracked uppercase label and optional trailing icon.
 * While [loading] it is disabled to prevent double submits.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: DrawableResource? = null,
    trailingIcon: DrawableResource? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().heightIn(min = ButtonMinHeight),
        shape = MaterialTheme.shapes.small,
        contentPadding = ButtonPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            ButtonLabel(text, leadingIcon, trailingIcon)
        }
    }
}

/** Quiet filled button (stone background) for the secondary path. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: DrawableResource? = null,
    trailingIcon: DrawableResource? = null,
    /** Compact variant (e.g. two buttons side by side): smaller label, no forced full width. */
    compact: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = (if (compact) modifier else modifier.fillMaxWidth()).heightIn(min = if (compact) 48.dp else ButtonMinHeight),
        shape = MaterialTheme.shapes.small,
        contentPadding = if (compact) PaddingValues(horizontal = 12.dp, vertical = 10.dp) else ButtonPadding,
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        if (compact) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (leadingIcon != null) AppIcon(leadingIcon, null, size = 16.dp)
                Text(text, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        } else {
            ButtonLabel(text, leadingIcon, trailingIcon)
        }
    }
}

/** Uppercase text link, e.g. "CONTINUE AS GUEST CLIENT ↗". */
@Composable
fun TextLinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: DrawableResource? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
            if (trailingIcon != null) AppIcon(trailingIcon, contentDescription = null, size = 16.dp)
        }
    }
}

@Composable
private fun ButtonLabel(text: String, leadingIcon: DrawableResource?, trailingIcon: DrawableResource?) {
    if (leadingIcon != null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppIcon(leadingIcon, contentDescription = null, size = 18.dp, tint = AppTheme.colors.accentOnPrimary)
            Text(text.uppercase(), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
        return
    }
    if (trailingIcon == null) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        return
    }
    // Label centred in the full width, icon pinned to the end — matches the editorial CTA.
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AppTheme.spacing.xl),
        )
        AppIcon(trailingIcon, contentDescription = null, size = 20.dp, modifier = Modifier.align(Alignment.CenterEnd))
    }
}
