package ai.reslt.kmpready.feature.auth.verify

import ai.reslt.kmpready.core.designsystem.component.AppIcon
import ai.reslt.kmpready.core.designsystem.component.AppIcons
import ai.reslt.kmpready.core.designsystem.component.CardTone
import ai.reslt.kmpready.core.designsystem.component.IconTile
import ai.reslt.kmpready.core.designsystem.component.InfoCard
import ai.reslt.kmpready.core.designsystem.component.OtpCodeInput
import ai.reslt.kmpready.core.designsystem.component.PrimaryButton
import ai.reslt.kmpready.core.designsystem.component.SecondaryButton
import ai.reslt.kmpready.core.designsystem.component.StatusPill
import ai.reslt.kmpready.core.designsystem.component.SurfaceCard
import ai.reslt.kmpready.core.designsystem.component.TrustBadgeRow
import ai.reslt.kmpready.core.designsystem.theme.AppTheme
import ai.reslt.kmpready.core.common.biometric.BiometricKind
import ai.reslt.kmpready.core.ui.ObserveAsEvents
import ai.reslt.kmpready.core.ui.asString
import ai.reslt.kmpready.core.ui.resolve
import ai.reslt.kmpready.feature.auth.components.AuthHeader
import ai.reslt.kmpready.feature.auth.components.ConciergeHelpDialog
import ai.reslt.kmpready.feature.auth.resources.Res
import ai.reslt.kmpready.feature.auth.resources.verify_alternate_device
import ai.reslt.kmpready.feature.auth.resources.verify_back_to_sign_in
import ai.reslt.kmpready.feature.auth.resources.verify_badge_fido
import ai.reslt.kmpready.feature.auth.resources.verify_badge_iso
import ai.reslt.kmpready.feature.auth.resources.verify_badge_ssl
import ai.reslt.kmpready.feature.auth.resources.verify_biometrics_hint
import ai.reslt.kmpready.feature.auth.resources.verify_biometrics_hint_fingerprint
import ai.reslt.kmpready.feature.auth.resources.verify_biometrics_subtitle
import ai.reslt.kmpready.feature.auth.resources.verify_biometrics_title
import ai.reslt.kmpready.feature.auth.resources.verify_call_stylist
import ai.reslt.kmpready.feature.auth.resources.verify_code_description
import ai.reslt.kmpready.feature.auth.resources.verify_fallback_body
import ai.reslt.kmpready.feature.auth.resources.verify_fallback_eyebrow
import ai.reslt.kmpready.feature.auth.resources.verify_fallback_title
import ai.reslt.kmpready.feature.auth.resources.verify_ready_face
import ai.reslt.kmpready.feature.auth.resources.verify_ready_fingerprint
import ai.reslt.kmpready.feature.auth.resources.verify_ready_generic
import ai.reslt.kmpready.feature.auth.resources.verify_resend_in
import ai.reslt.kmpready.feature.auth.resources.verify_resend_now
import ai.reslt.kmpready.feature.auth.resources.verify_scan
import ai.reslt.kmpready.feature.auth.resources.verify_status
import ai.reslt.kmpready.feature.auth.resources.verify_submit
import ai.reslt.kmpready.feature.auth.resources.verify_subtitle
import ai.reslt.kmpready.feature.auth.resources.verify_title
import ai.reslt.kmpready.feature.auth.resources.verify_vault_body
import ai.reslt.kmpready.feature.auth.resources.verify_vault_title
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VerifyScreen(
    onNavigateBack: () -> Unit,
    viewModel: VerifyViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            VerifyEffect.NavigateBack -> onNavigateBack()
            is VerifyEffect.ShowMessage -> snackbar.showSnackbar(effect.text.resolve())
            is VerifyEffect.OpenUri -> runCatching { uriHandler.openUri(effect.uri) }
        }
    }
    VerifyContent(state, viewModel::onIntent, snackbar)
}

@Composable
fun VerifyContent(
    state: VerifyState,
    onIntent: (VerifyIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val spacing = AppTheme.spacing
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(
                Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    Modifier.widthIn(max = 480.dp).padding(horizontal = spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AuthHeader(onClose = { onIntent(VerifyIntent.Exit) }, onHelp = { onIntent(VerifyIntent.ShowHelp) })
                    Spacer(Modifier.height(spacing.md))
                    BiometricEmblem()
                    Spacer(Modifier.height(spacing.md))
                    StatusPill(stringResource(Res.string.verify_status))
                    Spacer(Modifier.height(spacing.md))
                    Text(
                        stringResource(Res.string.verify_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.height(spacing.xs))
                    Text(
                        stringResource(Res.string.verify_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(spacing.md))
                    IdentityPill(state.displayName, state.memberReference)
                    Spacer(Modifier.height(spacing.lg))

                    if (state.biometricKind != null) {
                        BiometricCard(state, onIntent)
                        Spacer(Modifier.height(spacing.md))
                    }
                    PasscodeCard(state, onIntent)
                    Spacer(Modifier.height(spacing.md))
                    InfoCard(
                        icon = AppIcons.Vault,
                        title = stringResource(Res.string.verify_vault_title),
                        body = stringResource(Res.string.verify_vault_body),
                        tone = CardTone.Muted,
                    )

                    Spacer(Modifier.height(spacing.lg))
                    PrimaryButton(
                        text = stringResource(if (state.isLocked) Res.string.verify_back_to_sign_in else Res.string.verify_submit),
                        onClick = { onIntent(VerifyIntent.Submit) },
                        enabled = state.isLocked || state.canSubmit,
                        loading = state.isVerifying,
                        leadingIcon = AppIcons.Lock,
                    )
                    Spacer(Modifier.height(spacing.md))
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                        SecondaryButton(
                            text = stringResource(Res.string.verify_alternate_device),
                            onClick = { onIntent(VerifyIntent.AlternateDevice) },
                            leadingIcon = AppIcons.Smartphone,
                            compact = true,
                            enabled = !state.isLocked,
                            modifier = Modifier.weight(1f),
                        )
                        if (state.canCallStylist) {
                            SecondaryButton(
                                text = stringResource(Res.string.verify_call_stylist),
                                onClick = { onIntent(VerifyIntent.CallStylist) },
                                leadingIcon = AppIcons.Headset,
                                compact = true,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Spacer(Modifier.height(spacing.xl))
                    // ⚠ Only ship these marks if the certifications are actually held.
                    TrustBadgeRow(
                        listOf(
                            AppIcons.Shield to stringResource(Res.string.verify_badge_iso),
                            AppIcons.Fingerprint to stringResource(Res.string.verify_badge_fido),
                            AppIcons.Lock to stringResource(Res.string.verify_badge_ssl),
                        ),
                    )
                    Spacer(Modifier.height(spacing.xl))
                }
            }
            SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
        }
    }
    if (state.isHelpVisible) ConciergeHelpDialog(onDismiss = { onIntent(VerifyIntent.DismissHelp) })
}

@Composable
private fun IdentityPill(name: String, reference: String) {
    Row(
        Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        AppIcon(AppIcons.Shield, null, size = 16.dp, tint = AppTheme.colors.accent)
        Text(name, style = MaterialTheme.typography.bodyMedium)
        Text("(#$reference)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BiometricCard(state: VerifyState, onIntent: (VerifyIntent) -> Unit) {
    val ready = when (state.biometricKind) {
        BiometricKind.Face -> Res.string.verify_ready_face
        BiometricKind.Fingerprint -> Res.string.verify_ready_fingerprint
        else -> Res.string.verify_ready_generic
    }
    SurfaceCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            IconTile(AppIcons.FaceScan)
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.verify_biometrics_title).uppercase(), style = MaterialTheme.typography.labelMedium)
                Text(
                    stringResource(Res.string.verify_biometrics_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusPill(stringResource(ready), emphasized = true)
        }
        ScannerViewport(active = state.isScanning)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            Text(
                stringResource(
                    if (state.biometricKind == BiometricKind.Fingerprint) Res.string.verify_biometrics_hint_fingerprint
                    else Res.string.verify_biometrics_hint,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(
                text = stringResource(Res.string.verify_scan),
                onClick = { onIntent(VerifyIntent.Scan) },
                leadingIcon = AppIcons.Sensors,
                compact = true,
                enabled = !state.isBusy && !state.isLocked,
            )
        }
    }
}

@Composable
private fun PasscodeCard(state: VerifyState, onIntent: (VerifyIntent) -> Unit) {
    SurfaceCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(Res.string.verify_fallback_eyebrow).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(stringResource(Res.string.verify_fallback_title, state.codeLength), style = MaterialTheme.typography.titleLarge)
            }
            AppIcon(AppIcons.Keypad, null, size = 22.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            stringResource(Res.string.verify_fallback_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OtpCodeInput(
            value = state.code,
            onValueChange = { onIntent(VerifyIntent.CodeChanged(it)) },
            contentDescription = stringResource(Res.string.verify_code_description, state.codeLength),
            length = state.codeLength,
            isError = state.codeError != null,
            enabled = !state.isLocked,
            readOnly = state.isBusy,
            autoFocus = state.biometricKind == null, // with biometrics, let the user choose Scan first
            onDone = { onIntent(VerifyIntent.Submit) },
        )
        state.codeError?.let {
            Text(
                it.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
            ) {
                if (state.resendSecondsRemaining > 0) {
                    AppIcon(AppIcons.Schedule, null, size = 14.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        stringResource(Res.string.verify_resend_in),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        formatCountdown(state.resendSecondsRemaining),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
            val resendColor = if (state.canResend) AppTheme.colors.accent else AppTheme.colors.accent.copy(alpha = 0.45f)
            Text(
                stringResource(Res.string.verify_resend_now).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = resendColor,
                textDecoration = if (state.canResend) TextDecoration.Underline else null,
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .clickable(enabled = state.canResend, role = Role.Button) { onIntent(VerifyIntent.Resend) }
                    .padding(vertical = AppTheme.spacing.sm),
            )
        }
    }
}

private fun formatCountdown(seconds: Int): String =
    "${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"

@Preview
@Composable
private fun VerifyContentPreview() {
    AppTheme {
        VerifyContent(
            state = VerifyState(
                displayName = "Elena Vance",
                memberReference = "ATL-84920",
                code = "482",
                biometricKind = BiometricKind.Face,
                resendSecondsRemaining = 39,
            ),
            onIntent = {},
        )
    }
}
