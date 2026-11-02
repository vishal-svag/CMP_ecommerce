package ai.reslt.kmpready.feature.auth.welcome

import ai.reslt.kmpready.core.designsystem.component.AppIcons
import ai.reslt.kmpready.core.designsystem.component.EyebrowLabel
import ai.reslt.kmpready.core.designsystem.component.OverlayChip
import ai.reslt.kmpready.core.designsystem.component.PrimaryButton
import ai.reslt.kmpready.core.designsystem.component.SecondaryButton
import ai.reslt.kmpready.core.designsystem.component.Stat
import ai.reslt.kmpready.core.designsystem.component.StatStrip
import ai.reslt.kmpready.core.designsystem.component.TextLinkButton
import ai.reslt.kmpready.core.designsystem.component.TrustFootnote
import ai.reslt.kmpready.core.designsystem.theme.AppTheme
import ai.reslt.kmpready.core.ui.ObserveAsEvents
import ai.reslt.kmpready.feature.auth.components.AuthHeader
import ai.reslt.kmpready.feature.auth.components.ConciergeHelpDialog
import ai.reslt.kmpready.feature.auth.resources.Res
import ai.reslt.kmpready.feature.auth.resources.atelier_hero
import ai.reslt.kmpready.feature.auth.resources.welcome_badge
import ai.reslt.kmpready.feature.auth.resources.welcome_body
import ai.reslt.kmpready.feature.auth.resources.welcome_eyebrow
import ai.reslt.kmpready.feature.auth.resources.welcome_guest
import ai.reslt.kmpready.feature.auth.resources.welcome_register
import ai.reslt.kmpready.feature.auth.resources.welcome_salon
import ai.reslt.kmpready.feature.auth.resources.welcome_salon_cities
import ai.reslt.kmpready.feature.auth.resources.welcome_sign_in
import ai.reslt.kmpready.feature.auth.resources.welcome_stat_access_label
import ai.reslt.kmpready.feature.auth.resources.welcome_stat_access_value
import ai.reslt.kmpready.feature.auth.resources.welcome_stat_concierge_label
import ai.reslt.kmpready.feature.auth.resources.welcome_stat_concierge_value
import ai.reslt.kmpready.feature.auth.resources.welcome_stat_edition_label
import ai.reslt.kmpready.feature.auth.resources.welcome_stat_edition_value
import ai.reslt.kmpready.feature.auth.resources.welcome_title
import ai.reslt.kmpready.feature.auth.resources.welcome_trust
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val ContentMaxWidth = 480.dp

@Composable
fun WelcomeScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: WelcomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            WelcomeEffect.NavigateToSignIn -> onNavigateToSignIn()
            WelcomeEffect.NavigateToRegister -> onNavigateToRegister()
        }
    }
    WelcomeContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun WelcomeContent(state: WelcomeState, onIntent: (WelcomeIntent) -> Unit) {
    val spacing = AppTheme.spacing
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = ContentMaxWidth).padding(horizontal = spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AuthHeader(onClose = { onIntent(WelcomeIntent.Close) }, onHelp = { onIntent(WelcomeIntent.ShowHelp) })
                Spacer(Modifier.height(spacing.sm))
                HeroCard()
                Spacer(Modifier.height(spacing.xl))

                EyebrowLabel(stringResource(Res.string.welcome_eyebrow))
                Spacer(Modifier.height(spacing.sm))
                Text(
                    text = stringResource(Res.string.welcome_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(spacing.sm))
                Text(
                    text = stringResource(Res.string.welcome_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(spacing.lg))

                StatStrip(
                    stats = listOf(
                        Stat(stringResource(Res.string.welcome_stat_edition_label), stringResource(Res.string.welcome_stat_edition_value)),
                        Stat(stringResource(Res.string.welcome_stat_concierge_label), stringResource(Res.string.welcome_stat_concierge_value)),
                        Stat(stringResource(Res.string.welcome_stat_access_label), stringResource(Res.string.welcome_stat_access_value)),
                    ),
                )
                Spacer(Modifier.height(spacing.xl))

                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    PrimaryButton(
                        text = stringResource(Res.string.welcome_sign_in),
                        onClick = { onIntent(WelcomeIntent.SignIn) },
                        trailingIcon = AppIcons.ArrowForward,
                    )
                    SecondaryButton(
                        text = stringResource(Res.string.welcome_register),
                        onClick = { onIntent(WelcomeIntent.Register) },
                    )
                }
                Spacer(Modifier.height(spacing.sm))
                TextLinkButton(
                    text = stringResource(Res.string.welcome_guest),
                    onClick = { onIntent(WelcomeIntent.ContinueAsGuest) },
                    trailingIcon = AppIcons.NorthEast,
                )
                Spacer(Modifier.height(spacing.lg))
                TrustFootnote(stringResource(Res.string.welcome_trust))
                Spacer(Modifier.height(spacing.xl))
            }
        }
    }

    if (state.isHelpVisible) {
        ConciergeHelpDialog(onDismiss = { onIntent(WelcomeIntent.DismissHelp) })
    }
}

@Composable
private fun HeroCard() {
    val shape = MaterialTheme.shapes.large
    val fade = MaterialTheme.colorScheme.background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .shadow(elevation = 6.dp, shape = shape, clip = false)
            .clip(shape),
    ) {
        Image(
            painter = painterResource(Res.drawable.atelier_hero),
            contentDescription = null, // decorative campaign art
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Soft fade so the card bleeds into the page, as in the editorial layout.
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0.62f to Color.Transparent, 1f to fade.copy(alpha = 0.85f))),
        )
        OverlayChip(
            text = stringResource(Res.string.welcome_badge),
            leadingIcon = AppIcons.Award,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = AppTheme.spacing.md, start = AppTheme.spacing.md, end = AppTheme.spacing.md),
        )
        SalonTag(modifier = Modifier.align(Alignment.BottomEnd).padding(AppTheme.spacing.md))
    }
}

@Composable
private fun SalonTag(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(AppTheme.colors.scrim, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xs)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            stringResource(Res.string.welcome_salon).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(Res.string.welcome_salon_cities).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.accent,
        )
    }
}

@Preview
@Composable
private fun WelcomeContentPreview() {
    AppTheme { WelcomeContent(state = WelcomeState(), onIntent = {}) }
}
