package ai.reslt.kmpready.feature.auth.register

import ai.reslt.kmpready.core.designsystem.component.AppIconButton
import ai.reslt.kmpready.core.designsystem.component.AppIcons
import ai.reslt.kmpready.core.designsystem.component.EyebrowLabel
import ai.reslt.kmpready.core.designsystem.component.SecondaryButton
import ai.reslt.kmpready.core.designsystem.theme.AppTheme
import ai.reslt.kmpready.feature.auth.resources.Res
import ai.reslt.kmpready.feature.auth.resources.back
import ai.reslt.kmpready.feature.auth.resources.register_back_to_sign_in
import ai.reslt.kmpready.feature.auth.resources.register_body
import ai.reslt.kmpready.feature.auth.resources.register_eyebrow
import ai.reslt.kmpready.feature.auth.resources.register_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

/**
 * Placeholder until the membership-request API exists. Kept as its own route so the real
 * form (contract + ViewModel) can drop in without touching navigation.
 */
@Composable
fun RegisterScreen(onBack: () -> Unit, onSignIn: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            AppIconButton(
                icon = AppIcons.ArrowBack,
                contentDescription = stringResource(Res.string.back),
                onClick = onBack,
                modifier = Modifier.align(Alignment.Start).padding(AppTheme.spacing.xs),
            )
            Column(
                modifier = Modifier.weight(1f).widthIn(max = 480.dp).padding(horizontal = AppTheme.spacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EyebrowLabel(stringResource(Res.string.register_eyebrow))
                Text(stringResource(Res.string.register_title), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text(
                    stringResource(Res.string.register_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                SecondaryButton(
                    text = stringResource(Res.string.register_back_to_sign_in),
                    onClick = onSignIn,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
