package ai.reslt.kmpready.core.designsystem.component

import ai.reslt.kmpready.core.designsystem.resources.Res
import ai.reslt.kmpready.core.designsystem.resources.ic_account
import ai.reslt.kmpready.core.designsystem.resources.ic_concierge
import ai.reslt.kmpready.core.designsystem.resources.ic_face_scan
import ai.reslt.kmpready.core.designsystem.resources.ic_fingerprint
import ai.reslt.kmpready.core.designsystem.resources.ic_headset
import ai.reslt.kmpready.core.designsystem.resources.ic_keypad
import ai.reslt.kmpready.core.designsystem.resources.ic_schedule
import ai.reslt.kmpready.core.designsystem.resources.ic_sensors
import ai.reslt.kmpready.core.designsystem.resources.ic_shield
import ai.reslt.kmpready.core.designsystem.resources.ic_smartphone
import ai.reslt.kmpready.core.designsystem.resources.ic_vault
import ai.reslt.kmpready.core.designsystem.resources.ic_visibility
import ai.reslt.kmpready.core.designsystem.resources.ic_visibility_off
import ai.reslt.kmpready.core.designsystem.resources.ic_arrow_back
import ai.reslt.kmpready.core.designsystem.resources.ic_arrow_forward
import ai.reslt.kmpready.core.designsystem.resources.ic_award
import ai.reslt.kmpready.core.designsystem.resources.ic_close
import ai.reslt.kmpready.core.designsystem.resources.ic_help
import ai.reslt.kmpready.core.designsystem.resources.ic_lock
import ai.reslt.kmpready.core.designsystem.resources.ic_north_east
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** The icon set. Vector drawables (Material Symbols paths) — no icon-font dependency. */
object AppIcons {
    val Close: DrawableResource get() = Res.drawable.ic_close
    val Help: DrawableResource get() = Res.drawable.ic_help
    val ArrowForward: DrawableResource get() = Res.drawable.ic_arrow_forward
    val ArrowBack: DrawableResource get() = Res.drawable.ic_arrow_back
    val NorthEast: DrawableResource get() = Res.drawable.ic_north_east
    val Lock: DrawableResource get() = Res.drawable.ic_lock
    val Award: DrawableResource get() = Res.drawable.ic_award
    val Account: DrawableResource get() = Res.drawable.ic_account
    val Concierge: DrawableResource get() = Res.drawable.ic_concierge
    val FaceScan: DrawableResource get() = Res.drawable.ic_face_scan
    val Fingerprint: DrawableResource get() = Res.drawable.ic_fingerprint
    val Headset: DrawableResource get() = Res.drawable.ic_headset
    val Keypad: DrawableResource get() = Res.drawable.ic_keypad
    val Schedule: DrawableResource get() = Res.drawable.ic_schedule
    val Sensors: DrawableResource get() = Res.drawable.ic_sensors
    val Shield: DrawableResource get() = Res.drawable.ic_shield
    val Smartphone: DrawableResource get() = Res.drawable.ic_smartphone
    val Vault: DrawableResource get() = Res.drawable.ic_vault
    val Visibility: DrawableResource get() = Res.drawable.ic_visibility
    val VisibilityOff: DrawableResource get() = Res.drawable.ic_visibility_off

    /**
     * Neutral placeholder for the "Sign in with Apple" row. Apple's guidelines require their
     * official logo asset — download it from developer.apple.com/design/resources and replace.
     */
    val AppleSignInPlaceholder: DrawableResource get() = Res.drawable.ic_account
}

@Composable
fun AppIcon(
    icon: DrawableResource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size),
    )
}

/** 48dp touch target, 22dp glyph. */
@Composable
fun AppIconButton(
    icon: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        AppIcon(icon, contentDescription, size = 22.dp)
    }
}
