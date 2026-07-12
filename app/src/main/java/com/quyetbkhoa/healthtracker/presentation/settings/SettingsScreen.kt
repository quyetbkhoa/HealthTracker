package com.quyetbkhoa.healthtracker.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.AppThemeType
import com.quyetbkhoa.healthtracker.domain.model.AppLanguage
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton

@Composable
fun SettingsScreen(
    themeType: AppThemeType,
    onThemeChanged: (AppThemeType) -> Unit,
    selectedLanguage: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    onResetCompleted: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resetFailedMessage = stringResource(R.string.settings_reset_failed)

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.LanguageSelected -> onLanguageChanged(event.language)
                SettingsUiEvent.ResetCompleted -> onResetCompleted()
                SettingsUiEvent.ResetFailed -> Toast.makeText(
                    context,
                    resetFailedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    if (uiState.showResetConfirmation) {
        ResetConfirmationDialog(
            isResetting = uiState.isResetting,
            onConfirm = { viewModel.onAction(SettingsAction.ConfirmReset) },
            onDismiss = { viewModel.onAction(SettingsAction.CancelReset) }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.screen_settings),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Dimens.spaceExtraLarge))

        Text(
            text = stringResource(id = R.string.settings_select_theme),
            color = MaterialTheme.colorScheme.onBackground
        )

        AppThemeType.entries.forEach { type ->
            val labelRes = when (type) {
                AppThemeType.LIGHT -> R.string.theme_light
                AppThemeType.DARK -> R.string.theme_dark
                AppThemeType.PINK -> R.string.theme_pink
                AppThemeType.SYSTEM -> R.string.theme_system
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = themeType == type,
                    onClick = { onThemeChanged(type) }
                )
                Text(
                    text = stringResource(id = labelRes),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spaceExtraLarge))

        Text(
            text = stringResource(id = R.string.settings_select_language),
            color = MaterialTheme.colorScheme.onBackground
        )

        AppLanguage.entries.forEach { language ->
            val labelRes = when (language) {
                AppLanguage.VIETNAMESE -> R.string.language_vietnamese
                AppLanguage.ENGLISH -> R.string.language_english
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedLanguage == language,
                    onClick = {
                        viewModel.onAction(SettingsAction.SelectLanguage(language))
                    }
                )
                Text(
                    text = stringResource(id = labelRes),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spaceExtraLarge))

        HealthPrimaryButton(onClick = onNavigateToProfile) {
            Text(text = stringResource(id = R.string.profile_settings_open))
        }

        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

        HealthPrimaryButton(onClick = onNavigateBack) {
            Text(text = stringResource(id = R.string.settings_back))
        }

        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

        HealthPrimaryButton(
            onClick = { viewModel.onAction(SettingsAction.RequestReset) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text(text = stringResource(R.string.settings_reset_data))
        }
    }
}

@Composable
private fun ResetConfirmationDialog(
    isResetting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isResetting) onDismiss() },
        title = { Text(stringResource(R.string.settings_reset_title)) },
        text = { Text(stringResource(R.string.settings_reset_message)) },
        confirmButton = {
            HealthPrimaryButton(
                onClick = onConfirm,
                enabled = !isResetting
            ) {
                if (isResetting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimens.iconSizeMedium),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = Dimens.borderWidthThick
                    )
                } else {
                    Text(stringResource(R.string.settings_reset_confirm))
                }
            }
        },
        dismissButton = {
            Text(
                text = stringResource(R.string.settings_reset_cancel),
                modifier = Modifier
                    .clickable(enabled = !isResetting, onClick = onDismiss)
                    .padding(Dimens.spaceMedium),
                color = MaterialTheme.colorScheme.primary
            )
        }
    )
}
