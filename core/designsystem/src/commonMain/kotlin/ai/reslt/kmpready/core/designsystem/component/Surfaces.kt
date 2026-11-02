package ai.reslt.kmpready.core.designsystem.component

import ai.reslt.kmpready.core.designsystem.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource

enum class CardTone { Raised, Muted }

/** Content card: Raised = light surface + hairline, Muted = beige fill (reassurance / info). */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    tone: CardTone = CardTone.Raised,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val base = when (tone) {
        CardTone.Raised -> modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), shape)
        CardTone.Muted -> modifier.background(MaterialTheme.colorScheme.surfaceVariant, shape)
    }
    Column(
        modifier = base.fillMaxWidth().padding(AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
        content = content,
    )
}

/** Small rounded tile holding an icon (card headers, option rows). */
@Composable
fun IconTile(
    icon: DrawableResource,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    background: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Box(
        modifier = modifier.size(36.dp).background(background, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(icon, contentDescription = null, size = 20.dp, tint = tint)
    }
}

/** "• LABEL" — accent dot + tracked caption. */
@Composable
fun DotLabel(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(5.dp).background(AppTheme.colors.accent, CircleShape))
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color)
    }
}

/** Pill with a leading dot, e.g. "• CLIENT AUTHENTICATION" or "FACE ID READY". */
@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    Row(
        modifier = modifier
            .background(
                if (emphasized) AppTheme.colors.accentContainer else MaterialTheme.colorScheme.surfaceVariant,
                CircleShape,
            )
            .padding(horizontal = AppTheme.spacing.md, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(5.dp).background(AppTheme.colors.accent, CircleShape))
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = if (emphasized) AppTheme.colors.accent else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

/** Hairline — LABEL — hairline. */
@Composable
fun LabeledDivider(text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = AppTheme.spacing.md),
        )
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** Alternative sign-in method row: icon, title, trailing tag (optionally with accent dot). */
@Composable
fun AuthOptionRow(
    icon: DrawableResource,
    title: String,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tagWithDot: Boolean = false,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        AppIcon(icon, contentDescription = null, size = 22.dp)
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (tagWithDot) {
            DotLabel(tag)
        } else {
            Text(tag.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Accent-titled help card with an underlined call to action. */
@Composable
fun InfoCard(
    icon: DrawableResource,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    tone: CardTone = CardTone.Muted,
) {
    SurfaceCard(modifier = modifier, tone = tone) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            IconTile(icon, tint = AppTheme.colors.accent, background = MaterialTheme.colorScheme.surfaceContainerLowest)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.accent)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                if (actionLabel != null && onAction != null) {
                    Row(
                        modifier = Modifier
                            .clickable(role = Role.Button, onClick = onAction)
                            .heightIn(min = 40.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                    ) {
                        Text(
                            actionLabel.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            textDecoration = TextDecoration.Underline,
                        )
                        AppIcon(AppIcons.ArrowForward, null, size = 14.dp)
                    }
                }
            }
        }
    }
}

/** Monogram tile with a small accent lock badge (sign-in header). */
@Composable
fun MonogramBadge(letter: String, modifier: Modifier = Modifier) {
    Box(modifier.size(72.dp)) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.Center)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.large)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large),
            contentAlignment = Alignment.Center,
        ) {
            Text(letter, style = MaterialTheme.typography.headlineSmall)
        }
        Box(
            modifier = Modifier
                .size(22.dp)
                .align(Alignment.BottomEnd)
                .background(MaterialTheme.colorScheme.background, CircleShape)
                .padding(3.dp)
                .background(AppTheme.colors.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(AppIcons.Lock, null, size = 10.dp, tint = MaterialTheme.colorScheme.onSecondary)
        }
    }
}

/** "PARIS • MILANO • NEW YORK • GINZA" */
@Composable
fun FooterLine(items: List<String>, modifier: Modifier = Modifier) {
    Text(
        text = items.joinToString("  •  ") { it.uppercase() },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
    )
}

/** Row of small icon+label trust marks separated by dots. */
@Composable
fun TrustBadgeRow(badges: List<Pair<DrawableResource, String>>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        badges.forEachIndexed { index, (icon, label) ->
            if (index > 0) {
                Box(
                    Modifier.padding(horizontal = AppTheme.spacing.sm).size(3.dp)
                        .background(MaterialTheme.colorScheme.outline, CircleShape),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AppIcon(icon, null, size = 12.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

