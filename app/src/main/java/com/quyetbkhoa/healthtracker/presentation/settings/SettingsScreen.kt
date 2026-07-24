package com.quyetbkhoa.healthtracker.presentation.settings

import androidx.compose.runtime.Composable
import com.quyetbkhoa.healthtracker.domain.model.AppLanguage
import com.quyetbkhoa.healthtracker.domain.model.FontScale
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.model.ThemeMode

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    themeMode: ThemeMode,
    fontScale: FontScale,
    reminderSettings: ReminderSettings,
    hasExactAlarmAccess: Boolean,
    onThemeChanged: (ThemeMode) -> Unit,
    onFontScaleChanged: (FontScale) -> Unit,
    selectedLanguage: AppLanguage,
    onRemindersChanged: (Boolean) -> Unit,
    onReminderTimeChanged: (ReminderType, ReminderTime) -> Unit,
    onTestDinnerReminder: () -> Unit,
    onRequestExactAlarmAccess: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit
) {
    SettingsScreenContent(
        uiState = uiState,
        onAction = onAction,
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
