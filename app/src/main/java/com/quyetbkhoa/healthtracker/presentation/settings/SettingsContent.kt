package com.quyetbkhoa.healthtracker.presentation.settings

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthMarqueeText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.presentation.designsystem.Dimens
import com.quyetbkhoa.healthtracker.presentation.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.presentation.designsystem.Shape
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthIconText
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthOutlinedCard
import com.quyetbkhoa.healthtracker.domain.model.AppLanguage
import com.quyetbkhoa.healthtracker.domain.model.FontScale
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.model.ThemeMode

@Composable
internal fun SettingsScreenContent(
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
    if (uiState.showResetConfirmation) {
        ResetConfirmationDialog(
            isResetting = uiState.isResetting,
            onConfirm = { onAction(SettingsAction.ConfirmReset) },
            onDismiss = { onAction(SettingsAction.CancelReset) }
        )
    }

    if (uiState.showDemoDataConfirmation) {
        DemoDataConfirmationDialog(
            isLoading = uiState.isLoadingDemoData,
            onConfirm = { onAction(SettingsAction.ConfirmDemoData) },
            onDismiss = { onAction(SettingsAction.CancelDemoData) }
        )
    }

    SettingsContent(
        themeMode = themeMode,
        fontScale = fontScale,
        reminderSettings = reminderSettings,
        hasExactAlarmAccess = hasExactAlarmAccess,
        selectedLanguage = selectedLanguage,
        isResetting = uiState.isResetting,
        isLoadingDemoData = uiState.isLoadingDemoData,
        onThemeChanged = onThemeChanged,
        onFontScaleChanged = onFontScaleChanged,
        onLanguageChanged = { onAction(SettingsAction.SelectLanguage(it)) },
        onRemindersChanged = onRemindersChanged,
        onReminderTimeChanged = onReminderTimeChanged,
        onTestDinnerReminder = onTestDinnerReminder,
        onRequestExactAlarmAccess = onRequestExactAlarmAccess,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateBack = onNavigateBack,
        onLoadDemoData = { onAction(SettingsAction.RequestDemoData) },
        onReset = { onAction(SettingsAction.RequestReset) }
    )
}

@Composable
internal fun SettingsContent(
    themeMode: ThemeMode,
    fontScale: FontScale,
    reminderSettings: ReminderSettings,
    hasExactAlarmAccess: Boolean,
    selectedLanguage: AppLanguage,
    isResetting: Boolean,
    isLoadingDemoData: Boolean,
    onThemeChanged: (ThemeMode) -> Unit,
    onFontScaleChanged: (FontScale) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onRemindersChanged: (Boolean) -> Unit,
    onReminderTimeChanged: (ReminderType, ReminderTime) -> Unit,
    onTestDinnerReminder: () -> Unit,
    onRequestExactAlarmAccess: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    onLoadDemoData: () -> Unit,
    onReset: () -> Unit
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = { SettingsHeader(onNavigateBack) }
        ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clipToBounds()
                .padding(horizontal = Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
        ) {
//            item { SettingsHeader(onNavigateBack) }
            item {
                SettingsSectionCard(
                    icon = stringResource(R.string.settings_icon_profile),
                    title = stringResource(R.string.settings_account_section)
                ) {
                    SettingsNavigationCard(
                        icon = stringResource(R.string.settings_icon_profile),
                        title = stringResource(R.string.profile_settings_open),
                        subtitle = stringResource(R.string.settings_profile_description),
                        onClick = onNavigateToProfile
                    )
                }
            }
            item {
                SettingsSectionCard(
                    icon = stringResource(R.string.settings_icon_appearance),
                    title = stringResource(R.string.settings_appearance_section)
                ) {

                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                            ThemeChoice(
                                type = ThemeMode.LIGHT,
                                selected = themeMode == ThemeMode.LIGHT,
                                modifier = Modifier.weight(1f),
                                onClick = onThemeChanged
                            )
                            ThemeChoice(
                                type = ThemeMode.DARK,
                                selected = themeMode == ThemeMode.DARK,
                                modifier = Modifier.weight(1f),
                                onClick = onThemeChanged
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                            ThemeChoice(
                                type = ThemeMode.PINK,
                                selected = themeMode == ThemeMode.PINK,
                                modifier = Modifier.weight(1f),
                                onClick = onThemeChanged
                            )
                            ThemeChoice(
                                type = ThemeMode.SYSTEM,
                                selected = themeMode == ThemeMode.SYSTEM,
                                modifier = Modifier.weight(1f),
                                onClick = onThemeChanged
                            )
                        }
                        Spacer(Modifier.height(Dimens.spaceSmall))
                        Text(
                            text = stringResource(R.string.settings_font_size_title),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                            FontScale.entries.forEach { scale ->
                                FontSizeChoice(
                                    scale = scale,
                                    selected = fontScale == scale,
                                    modifier = Modifier.weight(1f),
                                    onClick = onFontScaleChanged
                                )
                            }
                        }
                    }
                }
            }
            item {
                SettingsSectionCard(
                    icon = stringResource(R.string.settings_icon_language),
                    title = stringResource(R.string.settings_language_section)
                ) {
                    LanguageChoice(
                        language = AppLanguage.VIETNAMESE,
                        selected = selectedLanguage == AppLanguage.VIETNAMESE,
                        onClick = onLanguageChanged
                    )
                    Spacer(Modifier.height(Dimens.spaceSmall))
                    LanguageChoice(
                        language = AppLanguage.ENGLISH,
                        selected = selectedLanguage == AppLanguage.ENGLISH,
                        onClick = onLanguageChanged
                    )
                }
            }
            item {
                SettingsSectionCard(
                    icon = stringResource(R.string.settings_icon_notifications),
                    title = stringResource(R.string.settings_notifications_section)
                ) {
                    ReminderSettingsCard(
                        settings = reminderSettings,
                        hasExactAlarmAccess = hasExactAlarmAccess,
                        onEnabledChange = onRemindersChanged,
                        onTimeChange = onReminderTimeChanged,
                        onTestDinnerReminder = onTestDinnerReminder,
                        onRequestExactAlarmAccess = onRequestExactAlarmAccess
                    )
                }
            }
            item {
                SettingsSectionCard(
                    icon = stringResource(R.string.settings_icon_data),
                    title = stringResource(R.string.settings_data_section)
                ) {
                    DemoDataCard(
                        isLoading = isLoadingDemoData,
                        onClick = onLoadDemoData
                    )
                    Spacer(Modifier.height(Dimens.spaceSmall))
                    DangerZoneCard(
                        isResetting = isResetting,
                        onClick = onReset
                    )
                }
            }
            item { Spacer(Modifier.height(Dimens.spaceLarge)) }
        }
    }
}

