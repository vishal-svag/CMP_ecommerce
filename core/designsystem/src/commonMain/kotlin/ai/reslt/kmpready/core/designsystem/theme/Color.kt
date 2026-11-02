package ai.reslt.kmpready.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Atelier palette — warm ivory, ink and a terracotta accent.
 * Components only read MaterialTheme.colorScheme / AppTheme.colors, so rebranding is this file.
 */
internal object AtelierColors {
    val Ink = Color(0xFF111111)
    val Ivory = Color(0xFFF7F4EF)
    val IvoryDeep = Color(0xFFEFEBE5)
    val Stone = Color(0xFFE8E4DE)
    val Taupe = Color(0xFF6E6862)
    val Hairline = Color(0xFFDCD6CE)
    val Terracotta = Color(0xFFB4552F)
    val TerracottaLight = Color(0xFFE2A184)
    val Charcoal = Color(0xFF161514)
    val CharcoalRaised = Color(0xFF22201E)
    val Error = Color(0xFFB3261E)
    val ErrorDark = Color(0xFFF2B8B5)
}

internal val LightColors = lightColorScheme(
    primary = AtelierColors.Ink,
    onPrimary = AtelierColors.Ivory,
    primaryContainer = AtelierColors.Stone,
    onPrimaryContainer = AtelierColors.Ink,
    secondary = AtelierColors.Terracotta,
    onSecondary = Color.White,
    secondaryContainer = AtelierColors.Stone,
    onSecondaryContainer = AtelierColors.Ink,
    tertiary = AtelierColors.Terracotta,
    background = AtelierColors.Ivory,
    onBackground = AtelierColors.Ink,
    surface = AtelierColors.Ivory,
    onSurface = AtelierColors.Ink,
    surfaceVariant = AtelierColors.IvoryDeep,
    onSurfaceVariant = AtelierColors.Taupe,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = AtelierColors.Ivory,
    surfaceContainer = AtelierColors.IvoryDeep,
    surfaceContainerHigh = AtelierColors.Stone,
    outline = AtelierColors.Taupe,
    outlineVariant = AtelierColors.Hairline,
    error = AtelierColors.Error,
)

internal val DarkColors = darkColorScheme(
    primary = AtelierColors.Ivory,
    onPrimary = AtelierColors.Ink,
    primaryContainer = AtelierColors.CharcoalRaised,
    onPrimaryContainer = AtelierColors.Ivory,
    secondary = AtelierColors.TerracottaLight,
    onSecondary = AtelierColors.Ink,
    secondaryContainer = AtelierColors.CharcoalRaised,
    onSecondaryContainer = AtelierColors.Ivory,
    tertiary = AtelierColors.TerracottaLight,
    background = AtelierColors.Charcoal,
    onBackground = AtelierColors.Ivory,
    surface = AtelierColors.Charcoal,
    onSurface = AtelierColors.Ivory,
    surfaceVariant = AtelierColors.CharcoalRaised,
    onSurfaceVariant = Color(0xFFB5AEA6),
    surfaceContainer = AtelierColors.CharcoalRaised,
    surfaceContainerHigh = Color(0xFF2C2A27),
    outline = Color(0xFF8C857D),
    outlineVariant = Color(0xFF3A3734),
    error = AtelierColors.ErrorDark,
)

/** Semantic colors Material3 doesn't model. */
data class ExtendedColors(
    val accent: Color,
    /** Accent tuned for use on top of the primary (ink) button. */
    val accentOnPrimary: Color,
    /** Soft accent fill, e.g. "FACE ID READY" pill. */
    val accentContainer: Color,
    val discount: Color,
    val success: Color,
    val rating: Color,
    /** Translucent fill for chips laid over photography. */
    val scrim: Color,
)

internal val LightExtendedColors = ExtendedColors(
    accent = AtelierColors.Terracotta,
    accentOnPrimary = Color(0xFFE0A07E),
    accentContainer = Color(0xFFF6E3D9),
    discount = AtelierColors.Terracotta,
    success = Color(0xFF2E6B3A),
    rating = Color(0xFF9A6B1F),
    scrim = AtelierColors.Ivory.copy(alpha = 0.88f),
)

internal val DarkExtendedColors = ExtendedColors(
    accent = AtelierColors.TerracottaLight,
    accentOnPrimary = AtelierColors.Terracotta,
    accentContainer = Color(0xFF3A2A22),
    discount = AtelierColors.TerracottaLight,
    success = Color(0xFF8CC59A),
    rating = Color(0xFFE3B866),
    scrim = AtelierColors.Charcoal.copy(alpha = 0.82f),
)
