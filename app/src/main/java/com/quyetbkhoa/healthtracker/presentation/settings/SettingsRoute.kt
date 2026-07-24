package com.quyetbkhoa.healthtracker.presentation.settings

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.domain.model.AppLanguage
import com.quyetbkhoa.healthtracker.domain.model.FontScale
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.model.ThemeMode

@Composable
fun SettingsRoute(
    themeMode: ThemeMode,
    fontScale: FontScale,
    reminderSettings: ReminderSettings,
    hasExactAlarmAccess: Boolean,
    onThemeChanged: (ThemeMode) -> Unit,
    onFontScaleChanged: (FontScale) -> Unit,
    selectedLanguage: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,
    onRemindersChanged: (Boolean) -> Unit,
    onReminderTimeChanged: (ReminderType, ReminderTime) -> Unit,
    onTestDinnerReminder: () -> Unit,
    onRequestExactAlarmAccess: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    onResetCompleted: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resetFailedMessage = stringResource(R.string.settings_reset_failed)
    val demoDataLoadedMessage = stringResource(R.string.settings_demo_data_loaded)
    val demoDataFailedMessage = stringResource(R.string.settings_demo_data_failed)

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
                SettingsUiEvent.DemoDataLoaded -> Toast.makeText(
                    context,
                    demoDataLoadedMessage,
                    Toast.LENGTH_LONG
                ).show()
                SettingsUiEvent.DemoDataFailed -> Toast.makeText(
                    context,
                    demoDataFailedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    SettingsScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        themeMode = themeMode,
        fontScale = fontScale,
        reminderSettings = reminderSettings,
        hasExactAlarmAccess = hasExactAlarmAccess,
        onThemeChanged = onThemeChanged,
        onFontScaleChanged = onFontScaleChanged,
        selectedLanguage = selectedLanguage,
        onRemindersChanged = onRemindersChanged,
        onReminderTimeChanged = onReminderTimeChanged,
        onTestDinnerReminder = onTestDinnerReminder,
        onRequestExactAlarmAccess = onRequestExactAlarmAccess,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateBack = onNavigateBack
    )
}
