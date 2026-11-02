package ai.reslt.kmpready.feature.auth.login

import ai.reslt.kmpready.core.designsystem.component.AppIcons
import ai.reslt.kmpready.core.designsystem.component.AuthOptionRow
import ai.reslt.kmpready.core.designsystem.component.DotLabel
import ai.reslt.kmpready.core.designsystem.component.FilledPasswordField
import ai.reslt.kmpready.core.designsystem.component.FilledTextField
import ai.reslt.kmpready.core.designsystem.component.FooterLine
import ai.reslt.kmpready.core.designsystem.component.InfoCard
import ai.reslt.kmpready.core.designsystem.component.LabeledCheckbox
import ai.reslt.kmpready.core.designsystem.component.LabeledDivider
import ai.reslt.kmpready.core.designsystem.component.MonogramBadge
import ai.reslt.kmpready.core.designsystem.component.PrimaryButton
import ai.reslt.kmpready.core.designsystem.component.SegmentedTabs
import ai.reslt.kmpready.core.designsystem.theme.AppTheme
import ai.reslt.kmpready.core.domain.model.Credentials
import ai.reslt.kmpready.core.domain.model.IdentifierType
import ai.reslt.kmpready.core.ui.ObserveAsEvents
import ai.reslt.kmpready.core.ui.UiText
import ai.reslt.kmpready.core.ui.asString
import ai.reslt.kmpready.core.ui.resolve
import ai.reslt.kmpready.feature.auth.components.AuthHeader
import ai.reslt.kmpready.feature.auth.components.ConciergeHelpDialog
import ai.reslt.kmpready.feature.auth.resources.Res
import ai.reslt.kmpready.feature.auth.resources.login_apple
import ai.reslt.kmpready.feature.auth.resources.login_apple_tag
import ai.reslt.kmpready.feature.auth.resources.login_biometric_enabled
import ai.reslt.kmpready.feature.auth.resources.login_cities
import ai.reslt.kmpready.feature.auth.resources.login_desk_action
import ai.reslt.kmpready.feature.auth.resources.login_desk_body
import ai.reslt.kmpready.feature.auth.resources.login_desk_title
import ai.reslt.kmpready.feature.auth.resources.login_forgot_key
import ai.reslt.kmpready.feature.auth.resources.login_hide_password
import ai.reslt.kmpready.feature.auth.resources.login_identifier_label
import ai.reslt.kmpready.feature.auth.resources.login_identifier_placeholder
import ai.reslt.kmpready.feature.auth.resources.login_member_label
import ai.reslt.kmpready.feature.auth.resources.login_member_placeholder
import ai.reslt.kmpready.feature.auth.resources.login_monogram
import ai.reslt.kmpready.feature.auth.resources.login_or
import ai.reslt.kmpready.feature.auth.resources.login_passkey
import ai.reslt.kmpready.feature.auth.resources.login_passkey_tag
import ai.reslt.kmpready.feature.auth.resources.login_password
import ai.reslt.kmpready.feature.auth.resources.login_password_placeholder
import ai.reslt.kmpready.feature.auth.resources.login_remember_me
import ai.reslt.kmpready.feature.auth.resources.login_show_password
import ai.reslt.kmpready.feature.auth.resources.login_submit
import ai.reslt.kmpready.feature.auth.resources.login_subtitle
import ai.reslt.kmpready.feature.auth.resources.login_tab_client
import ai.reslt.kmpready.feature.auth.resources.login_tab_member
import ai.reslt.kmpready.feature.auth.resources.login_title
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onNavigateToVerification: () -> Unit,
    sessionMessage: UiText? = null,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current
    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            LoginEffect.NavigateToVerification -> onNavigateToVerification()
            is LoginEffect.ShowMessage -> snackbar.showSnackbar(effect.text.resolve())
            is LoginEffect.OpenUri -> runCatching { uriHandler.openUri(effect.uri) }
        }
    }
    LoginContent(state, viewModel::onIntent, onBack, snackbar, sessionMessage)
}

@Composable
fun LoginContent(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    sessionMessage: UiText? = null,
) {
    val spacing = AppTheme.spacing
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        onIntent(LoginIntent.Submit)
    }
    val isMember = state.identifierType == IdentifierType.MemberNumber

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(
                modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 480.dp).padding(horizontal = spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AuthHeader(onClose = onBack, onHelp = { onIntent(LoginIntent.ShowHelp) })
                    Spacer(Modifier.height(spacing.lg))
                    MonogramBadge(stringResource(Res.string.login_monogram))
                    Spacer(Modifier.height(spacing.md))
                    Text(
                        stringResource(Res.string.login_title),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.height(spacing.xs))
                    Text(
                        stringResource(Res.string.login_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(spacing.xl))

                    SegmentedTabs(
                        options = listOf(stringResource(Res.string.login_tab_client), stringResource(Res.string.login_tab_member)),
                        selectedIndex = if (isMember) 1 else 0,
                        onSelect = { index ->
                            onIntent(LoginIntent.IdentifierTypeChanged(if (index == 1) IdentifierType.MemberNumber else IdentifierType.ClientIdOrEmail))
                        },
                        enabled = !state.isLoading,
                    )
                    Spacer(Modifier.height(spacing.lg))

                    FilledTextField(
                        value = state.identifier,
                        onValueChange = { onIntent(LoginIntent.IdentifierChanged(it)) },
                        label = stringResource(if (isMember) Res.string.login_member_label else Res.string.login_identifier_label),
                        placeholder = stringResource(if (isMember) Res.string.login_member_placeholder else Res.string.login_identifier_placeholder),
                        error = state.identifierError?.asString(),
                        enabled = !state.isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isMember) KeyboardType.Number else KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            autoCorrectEnabled = false,
                        ),
                    )
                    Spacer(Modifier.height(spacing.md))
                    FilledPasswordField(
                        value = state.password,
                        onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
                        label = stringResource(Res.string.login_password),
                        placeholder = stringResource(Res.string.login_password_placeholder),
                        visible = state.isPasswordVisible,
                        onToggleVisibility = { onIntent(LoginIntent.TogglePasswordVisibility) },
                        showDescription = stringResource(Res.string.login_show_password),
                        hideDescription = stringResource(Res.string.login_hide_password),
                        error = state.passwordError?.asString(),
                        enabled = !state.isLoading,
                        labelAction = stringResource(Res.string.login_forgot_key) to { onIntent(LoginIntent.ForgotKey) },
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                    )
                    Spacer(Modifier.height(spacing.sm))
                    LabeledCheckbox(
                        checked = state.rememberMe,
                        onCheckedChange = { onIntent(LoginIntent.RememberMeChanged(it)) },
                        label = stringResource(Res.string.login_remember_me, Credentials.REMEMBER_ME_DAYS),
                        enabled = !state.isLoading,
                        supporting = if (state.biometricKind != null) {
                            { DotLabel(stringResource(Res.string.login_biometric_enabled)) }
                        } else {
                            null
                        },
                    )
                    (state.formError ?: sessionMessage)?.let {
                        Spacer(Modifier.height(spacing.sm))
                        Text(
                            it.asString(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(Modifier.height(spacing.lg))
                    PrimaryButton(
                        text = stringResource(Res.string.login_submit),
                        onClick = submit,
                        enabled = state.canSubmit,
                        loading = state.isLoading,
                        leadingIcon = AppIcons.Shield,
                    )

                    Spacer(Modifier.height(spacing.xl))
                    LabeledDivider(stringResource(Res.string.login_or))
                    Spacer(Modifier.height(spacing.lg))
                    AuthOptionRow(
                        icon = AppIcons.AppleSignInPlaceholder,
                        title = stringResource(Res.string.login_apple),
                        tag = stringResource(Res.string.login_apple_tag),
                        onClick = { onIntent(LoginIntent.SignInWithApple) },
                        enabled = !state.isLoading,
                    )
                    Spacer(Modifier.height(spacing.sm))
                    AuthOptionRow(
                        icon = AppIcons.Fingerprint,
                        title = stringResource(Res.string.login_passkey),
                        tag = stringResource(Res.string.login_passkey_tag),
                        tagWithDot = true,
                        onClick = { onIntent(LoginIntent.SignInWithPasskey) },
                        enabled = !state.isLoading,
                    )

                    if (state.showConciergeDesk) {
                        Spacer(Modifier.height(spacing.xl))
                        InfoCard(
                            icon = AppIcons.Concierge,
                            title = stringResource(Res.string.login_desk_title),
                            body = stringResource(Res.string.login_desk_body),
                            actionLabel = stringResource(Res.string.login_desk_action),
                            onAction = { onIntent(LoginIntent.ContactConcierge) },
                        )
                    }
                    Spacer(Modifier.height(spacing.xl))
                    FooterLine(stringResource(Res.string.login_cities).split('|'))
                    Spacer(Modifier.height(spacing.xl))
                }
            }
            SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
        }
    }

    if (state.isHelpVisible) ConciergeHelpDialog(onDismiss = { onIntent(LoginIntent.DismissHelp) })
}

@Preview
@Composable
private fun LoginContentPreview() {
    AppTheme { LoginContent(state = LoginState(identifier = "emilys"), onIntent = {}, onBack = {}) }
}
