package ai.reslt.kmpready.core.designsystem.component

import ai.reslt.kmpready.core.designsystem.theme.AppTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow

/**
 * Stateless product tile. Takes pre-formatted strings so the design system
 * stays independent of domain models and locale/currency formatting.
 */
@Composable
fun ProductCard(
    title: String,
    imageUrl: String?,
    price: String,
    originalPrice: String?,
    discountLabel: String?,
    ratingLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outOfStockLabel: String? = null,
) {
    Card(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.medium) {
        RemoteImage(
            url = imageUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        )
        Column(
            modifier = Modifier.padding(AppTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(price, style = MaterialTheme.typography.titleMedium)
                if (originalPrice != null) {
                    Text(
                        text = originalPrice,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = TextDecoration.LineThrough,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                Text(ratingLabel, style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.rating)
                if (discountLabel != null) {
                    Text(discountLabel, style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.discount)
                }
            }
            if (outOfStockLabel != null) {
                Text(outOfStockLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
