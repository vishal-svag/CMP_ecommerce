package ai.reslt.kmpready.core.designsystem.component

import ai.reslt.kmpready.core.designsystem.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource

/** Two-line brand lockup: "ATELIER / MAISON DE MODE". */
@Composable
fun BrandWordmark(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) { heading() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title.uppercase(), style = AppTheme.type.wordmark, color = MaterialTheme.colorScheme.onBackground)
        Text(
            subtitle.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Accent overline flanked by hairlines: "—— EXCLUSIVE RETROSPECTIVE ——". */
@Composable
fun EyebrowLabel(text: String, modifier: Modifier = Modifier) {
    val accent = AppTheme.colors.accent
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        Box(Modifier.width(24.dp).height(1.dp).background(accent.copy(alpha = 0.6f)))
        Text(text.uppercase(), style = AppTheme.type.eyebrow, color = accent)
        Box(Modifier.width(24.dp).height(1.dp).background(accent.copy(alpha = 0.6f)))
    }
}

/** Pill chip for use over photography. */
@Composable
fun OverlayChip(text: String, modifier: Modifier = Modifier, leadingIcon: DrawableResource? = null) {
    Row(
        modifier = modifier
            .background(AppTheme.colors.scrim, CircleShape)
            .padding(horizontal = AppTheme.spacing.md, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (leadingIcon != null) AppIcon(leadingIcon, null, size = 14.dp, tint = AppTheme.colors.accent)
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Immutable
data class Stat(val label: String, val value: String)

/** "EDITION | CONCIERGE | ACCESS" strip with hairline dividers. */
@Composable
fun StatStrip(stats: List<Stat>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            .padding(vertical = AppTheme.spacing.md)
            .height(IntrinsicSize.Min),
    ) {
        stats.forEachIndexed { index, stat ->
            if (index > 0) {
                Box(
                    Modifier.width(1.dp).fillMaxHeight().padding(vertical = AppTheme.spacing.xs)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = AppTheme.spacing.xs)
                    .semantics(mergeDescendants = true) {},
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    stat.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Text(
                    stat.value,
                    style = AppTheme.type.statValue,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Small lock + caption reassurance line. */
@Composable
fun TrustFootnote(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        AppIcon(AppIcons.Lock, null, size = 14.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
