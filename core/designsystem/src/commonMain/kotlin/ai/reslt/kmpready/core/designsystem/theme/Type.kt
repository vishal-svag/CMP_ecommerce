package ai.reslt.kmpready.core.designsystem.theme

import ai.reslt.kmpready.core.designsystem.resources.Res
import ai.reslt.kmpready.core.designsystem.resources.bodoni_moda_medium
import ai.reslt.kmpready.core.designsystem.resources.bodoni_moda_regular
import ai.reslt.kmpready.core.designsystem.resources.bodoni_moda_semibold
import ai.reslt.kmpready.core.designsystem.resources.plus_jakarta_sans_medium
import ai.reslt.kmpready.core.designsystem.resources.plus_jakarta_sans_regular
import ai.reslt.kmpready.core.designsystem.resources.plus_jakarta_sans_semibold
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font

/**
 * Editorial pairing: Bodoni Moda (display/headlines) + Plus Jakarta Sans (UI/body/labels).
 * Both are SIL OFL 1.1 — licenses in core/designsystem/licenses.
 */
@Composable
internal fun serifFamily(): FontFamily = FontFamily(
    Font(Res.font.bodoni_moda_regular, FontWeight.Normal),
    Font(Res.font.bodoni_moda_medium, FontWeight.Medium),
    Font(Res.font.bodoni_moda_semibold, FontWeight.SemiBold),
)

@Composable
internal fun sansFamily(): FontFamily = FontFamily(
    Font(Res.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(Res.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(Res.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
)

@Composable
internal fun appTypography(serif: FontFamily, sans: FontFamily): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = serif, fontWeight = FontWeight.Normal),
        displayMedium = base.displayMedium.copy(fontFamily = serif, fontWeight = FontWeight.Normal),
        displaySmall = base.displaySmall.copy(fontFamily = serif, fontWeight = FontWeight.Normal),
        headlineLarge = base.headlineLarge.copy(fontFamily = serif, fontWeight = FontWeight.Normal),
        headlineMedium = TextStyle(fontFamily = serif, fontWeight = FontWeight.Normal, fontSize = 27.sp, lineHeight = 34.sp, letterSpacing = (-0.2).sp),
        headlineSmall = TextStyle(fontFamily = serif, fontWeight = FontWeight.Normal, fontSize = 23.sp, lineHeight = 30.sp),
        titleLarge = TextStyle(fontFamily = serif, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp),
        titleMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
        titleSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = sans, fontSize = 14.5.sp, lineHeight = 22.sp),
        bodyMedium = TextStyle(fontFamily = sans, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontFamily = sans, fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 12.5.sp, lineHeight = 16.sp, letterSpacing = 1.4.sp),
        labelMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 10.5.sp, lineHeight = 14.sp, letterSpacing = 1.3.sp),
        labelSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 9.5.sp, lineHeight = 12.sp, letterSpacing = 1.3.sp),
    )
}

/** Brand styles that don't map onto a Material3 slot. */
@Immutable
data class BrandTypography(
    /** "ATELIER" wordmark. */
    val wordmark: TextStyle,
    /** Small serif values, e.g. stat strip ("Automne XXIV"). */
    val statValue: TextStyle,
    /** Tracked uppercase eyebrow / overline. */
    val eyebrow: TextStyle,
)

internal fun brandTypography(serif: FontFamily, sans: FontFamily) = BrandTypography(
    wordmark = TextStyle(fontFamily = serif, fontWeight = FontWeight.Medium, fontSize = 21.sp, lineHeight = 24.sp, letterSpacing = 4.sp),
    statValue = TextStyle(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp),
    eyebrow = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 10.5.sp, lineHeight = 14.sp, letterSpacing = 1.8.sp),
)
