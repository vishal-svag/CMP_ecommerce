package ai.reslt.kmpready.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

private val LocalSpacing = staticCompositionLocalOf { Spacing() }
private val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
private val LocalBrandTypography = staticCompositionLocalOf<BrandTypography> {
    error("AppTheme not applied — wrap content in AppTheme { }")
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val serif = serifFamily()
    val sans = sansFamily()
    val typography = appTypography(serif, sans)
    val brandTypography = remember(serif, sans) { brandTypography(serif, sans) }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors,
        LocalBrandTypography provides brandTypography,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = typography,
            shapes = AppShapes,
            content = content,
        )
    }
}

/** Design tokens beyond Material: `AppTheme.spacing.lg`, `AppTheme.colors.accent`, `AppTheme.type.wordmark`. */
object AppTheme {
    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current
    val colors: ExtendedColors
        @Composable @ReadOnlyComposable get() = LocalExtendedColors.current
    val type: BrandTypography
        @Composable @ReadOnlyComposable get() = LocalBrandTypography.current
}
