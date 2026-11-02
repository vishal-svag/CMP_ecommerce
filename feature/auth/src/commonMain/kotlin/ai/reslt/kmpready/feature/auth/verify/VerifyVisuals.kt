package ai.reslt.kmpready.feature.auth.verify

import ai.reslt.kmpready.core.designsystem.component.AppIcon
import ai.reslt.kmpready.core.designsystem.component.AppIcons
import ai.reslt.kmpready.core.designsystem.theme.AppTheme
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Ink tile with a face glyph, ringed by a partial accent arc and a shield badge (decorative). */
@Composable
internal fun BiometricEmblem(modifier: Modifier = Modifier) {
    val accent = AppTheme.colors.accent
    val track = MaterialTheme.colorScheme.outlineVariant
    Box(modifier.size(104.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 2.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(accent, 120f, 250f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Box(
            Modifier.size(68.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(AppIcons.FaceScan, null, size = 30.dp, tint = AppTheme.colors.accentOnPrimary)
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 12.dp)
                .size(24.dp)
                .background(MaterialTheme.colorScheme.background, CircleShape)
                .padding(2.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(AppIcons.Shield, null, size = 14.dp, tint = accent)
        }
    }
}

/** Stylised scanner viewport: soft stone gradient, corner brackets and a sweeping accent line. */
@Composable
internal fun ScannerViewport(active: Boolean, modifier: Modifier = Modifier) {
    val accent = AppTheme.colors.accent
    val bracket = MaterialTheme.colorScheme.surfaceContainerLowest
    val base = MaterialTheme.colorScheme.surfaceVariant
    val deep = MaterialTheme.colorScheme.outlineVariant
    val transition = rememberInfiniteTransition(label = "scan")
    val sweep by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(if (active) 900 else 2_400, easing = LinearEasing), RepeatMode.Reverse),
        label = "sweep",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Brush.horizontalGradient(listOf(deep, base, deep))),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val pad = 16.dp.toPx()
            val len = 18.dp.toPx()
            val w = 2.dp.toPx()
            val l = size.width * 0.32f
            val r = size.width * 0.68f
            val t = pad
            val b = size.height - pad
            // Corner brackets around the "face" frame.
            listOf(
                Offset(l, t) to Offset(1f, 1f),
                Offset(r, t) to Offset(-1f, 1f),
                Offset(l, b) to Offset(1f, -1f),
                Offset(r, b) to Offset(-1f, -1f),
            ).forEach { (corner, dir) ->
                drawLine(bracket, corner, corner + Offset(len * dir.x, 0f), w, StrokeCap.Round)
                drawLine(bracket, corner, corner + Offset(0f, len * dir.y), w, StrokeCap.Round)
            }
            // Scan line with a soft glow.
            val y = size.height * sweep
            drawRect(
                Brush.verticalGradient(listOf(Color.Transparent, accent.copy(alpha = 0.25f), Color.Transparent), y - 10f, y + 10f),
                topLeft = Offset(0f, y - 10f),
                size = Size(size.width, 20f),
            )
            drawLine(accent, Offset(0f, y), Offset(size.width, y), 1.5.dp.toPx())
        }
    }
}
