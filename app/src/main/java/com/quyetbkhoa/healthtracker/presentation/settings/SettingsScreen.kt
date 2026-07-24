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
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthMarqueeText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.AppFontSize
import com.quyetbkhoa.healthtracker.core.designsystem.AppThemeType
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthIconText
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthOutlinedCard
import com.quyetbkhoa.healthtracker.domain.model.AppLanguage
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ReminderType

@Composable
fun SettingsScreen(
    themeType: AppThemeType,
    fontSize: AppFontSize,
    reminderSettings: ReminderSettings,
    hasExactAlarmAccess: Boolean,
    onThemeChanged: (AppThemeType) -> Unit,
    onFontSizeChanged: (AppFontSize) -> Unit,
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
    val context = androidx.compose.ui.platform.LocalContext.current
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

    if (uiState.showResetConfirmation) {
        ResetConfirmationDialog(
            isResetting = uiState.isResetting,
            onConfirm = { viewModel.onAction(SettingsAction.ConfirmReset) },
            onDismiss = { viewModel.onAction(SettingsAction.CancelReset) }
        )
    }

    if (uiState.showDemoDataConfirmation) {
        DemoDataConfirmationDialog(
            isLoading = uiState.isLoadingDemoData,
            onConfirm = { viewModel.onAction(SettingsAction.ConfirmDemoData) },
            onDismiss = { viewModel.onAction(SettingsAction.CancelDemoData) }
        )
    }

    SettingsContent(
        themeType = themeType,
        fontSize = fontSize,
        reminderSettings = reminderSettings,
        hasExactAlarmAccess = hasExactAlarmAccess,
        selectedLanguage = selectedLanguage,
        isResetting = uiState.isResetting,
        isLoadingDemoData = uiState.isLoadingDemoData,
        onThemeChanged = onThemeChanged,
        onFontSizeChanged = onFontSizeChanged,
        onLanguageChanged = { viewModel.onAction(SettingsAction.SelectLanguage(it)) },
        onRemindersChanged = onRemindersChanged,
        onReminderTimeChanged = onReminderTimeChanged,
        onTestDinnerReminder = onTestDinnerReminder,
        onRequestExactAlarmAccess = onRequestExactAlarmAccess,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateBack = onNavigateBack,
        onLoadDemoData = { viewModel.onAction(SettingsAction.RequestDemoData) },
        onReset = { viewModel.onAction(SettingsAction.RequestReset) }
    )
}

@Composable
private fun SettingsContent(
    themeType: AppThemeType,
    fontSize: AppFontSize,
    reminderSettings: ReminderSettings,
    hasExactAlarmAccess: Boolean,
    selectedLanguage: AppLanguage,
    isResetting: Boolean,
    isLoadingDemoData: Boolean,
    onThemeChanged: (AppThemeType) -> Unit,
    onFontSizeChanged: (AppFontSize) -> Unit,
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
                                type = AppThemeType.LIGHT,
                                selected = themeType == AppThemeType.LIGHT,
                                modifier = Modifier.weight(1f),
                                onClick = onThemeChanged
                            )
                            ThemeChoice(
                                type = AppThemeType.DARK,
                                selected = themeType == AppThemeType.DARK,
                                modifier = Modifier.weight(1f),
                                onClick = onThemeChanged
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                            ThemeChoice(
                                type = AppThemeType.PINK,
                                selected = themeType == AppThemeType.PINK,
                                modifier = Modifier.weight(1f),
                                onClick = onThemeChanged
                            )
                            ThemeChoice(
                                type = AppThemeType.SYSTEM,
                                selected = themeType == AppThemeType.SYSTEM,
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
                            AppFontSize.entries.forEach { size ->
                                FontSizeChoice(
                                    size = size,
                                    selected = fontSize == size,
                                    modifier = Modifier.weight(1f),
                                    onClick = onFontSizeChanged
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

@Composable
private fun ReminderSettingsCard(
    settings: ReminderSettings,
    hasExactAlarmAccess: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (ReminderType, ReminderTime) -> Unit,
    onTestDinnerReminder: () -> Unit,
    onRequestExactAlarmAccess: () -> Unit
) {
    val context = LocalContext.current
    val testScheduledMessage = stringResource(R.string.settings_notification_test_scheduled)
    HealthOutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.large
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEnabledChange(!settings.isEnabled) }
                    .padding(Dimens.spaceMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_notifications_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_notifications_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.isEnabled,
                    onCheckedChange = null
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spaceMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        if (hasExactAlarmAccess) R.string.settings_exact_alarm_enabled
                        else R.string.settings_exact_alarm_disabled
                    ),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                TextButton(
                    onClick = onRequestExactAlarmAccess,
                    enabled = !hasExactAlarmAccess
                ) {
                    Text(
                        text = stringResource(
                            if (hasExactAlarmAccess) R.string.settings_exact_alarm_granted
                            else R.string.settings_exact_alarm_allow
                        ),
                        maxLines = 1
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            HealthPrimaryButton(
                onClick = {
                    onTestDinnerReminder()
                    Toast.makeText(
                        context,
                        testScheduledMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.spaceMedium)
            ) {
                Text(stringResource(R.string.settings_notification_test_button))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ReminderTimeRow(
                type = ReminderType.BREAKFAST,
                time = settings.breakfast,
                onTimeChange = onTimeChange
            )
            ReminderTimeRow(
                type = ReminderType.LUNCH,
                time = settings.lunch,
                onTimeChange = onTimeChange
            )
            ReminderTimeRow(
                type = ReminderType.DINNER,
                time = settings.dinner,
                onTimeChange = onTimeChange
            )
            ReminderTimeRow(
                type = ReminderType.ACTIVITY,
                time = settings.activity,
                onTimeChange = onTimeChange
            )
        }
    }
}

@Composable
private fun ReminderTimeRow(
    type: ReminderType,
    time: ReminderTime,
    onTimeChange: (ReminderType, ReminderTime) -> Unit
) {
    val context = LocalContext.current
    val label = stringResource(
        when (type) {
            ReminderType.BREAKFAST -> R.string.settings_notification_breakfast
            ReminderType.LUNCH -> R.string.settings_notification_lunch
            ReminderType.DINNER -> R.string.settings_notification_dinner
            ReminderType.ACTIVITY -> R.string.settings_notification_activity
        }
    )
    val timeText = stringResource(
        R.string.settings_notification_time_value,
        time.hour,
        time.minute
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onTimeChange(type, ReminderTime(hour = hour, minute = minute))
                    },
                    time.hour,
                    time.minute,
                    true
                ).show()
            }
            .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = timeText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        HealthIconText(
            text = stringResource(R.string.settings_icon_chevron),
            modifier = Modifier.padding(start = Dimens.spaceSmall),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsHeader(onNavigateBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = Dimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back)
                )
            }
            Text(
                text = stringResource(R.string.screen_settings),
                modifier = Modifier.padding(start = Dimens.spaceSmall),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    icon: String,
    title: String,
    content: @Composable ColumnScope.() -> Unit
){
    HealthElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.large
    ){
        Column(
            modifier = Modifier.padding(Dimens.spaceMedium)
        ){
            //title
            Row(verticalAlignment = Alignment.CenterVertically) {
                HealthIconText(text = icon, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = title,
                    modifier = Modifier.padding(start = Dimens.spaceSmall),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            //content
            content()


        }
    }
}

@Composable
private fun SettingsNavigationCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    HealthOutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Shape.large
    ) {
        Row(
            modifier = Modifier.padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIconBubble(icon = icon)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimens.spaceMedium)
            ) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HealthIconText(
                text = stringResource(R.string.settings_icon_chevron),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ThemeChoice(
    type: AppThemeType,
    selected: Boolean,
    modifier: Modifier,
    onClick: (AppThemeType) -> Unit
) {
    val label = stringResource(
        when (type) {
            AppThemeType.LIGHT -> R.string.theme_light
            AppThemeType.DARK -> R.string.theme_dark
            AppThemeType.PINK -> R.string.theme_pink
            AppThemeType.SYSTEM -> R.string.theme_system
        }
    )
    val icon = stringResource(
        when (type) {
            AppThemeType.LIGHT -> R.string.settings_icon_light
            AppThemeType.DARK -> R.string.settings_icon_dark
            AppThemeType.PINK -> R.string.settings_icon_pink
            AppThemeType.SYSTEM -> R.string.settings_icon_system
        }
    )
    HealthCard(
        modifier = modifier.clickable { onClick(type) },
        shape = Shape.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.buttonHeightLarge)
                .padding(Dimens.spaceMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            HealthIconText(
                text = icon,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FontSizeChoice(
    size: AppFontSize,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (AppFontSize) -> Unit
) {
    val label = stringResource(
        when (size) {
            AppFontSize.SMALL -> R.string.settings_font_size_small
            AppFontSize.MEDIUM -> R.string.settings_font_size_medium
            AppFontSize.LARGE -> R.string.settings_font_size_large
        }
    )
    val sampleStyle = when (size) {
        AppFontSize.SMALL -> MaterialTheme.typography.bodyMedium
        AppFontSize.MEDIUM -> MaterialTheme.typography.titleMedium
        AppFontSize.LARGE -> MaterialTheme.typography.headlineSmall
    }
    HealthCard(
        modifier = modifier
            .height(Dimens.buttonHeightLarge)
            .clickable { onClick(size) },
        shape = Shape.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(Dimens.spaceSmall),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.settings_font_size_sample),
                    style = sampleStyle,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LanguageChoice(
    language: AppLanguage,
    selected: Boolean,
    onClick: (AppLanguage) -> Unit
) {
    val label = stringResource(
        if (language == AppLanguage.VIETNAMESE) R.string.language_vietnamese
        else R.string.language_english
    )
    val nativeLabel = stringResource(
        if (language == AppLanguage.VIETNAMESE) R.string.settings_vietnamese_native
        else R.string.settings_english_native
    )
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.buttonHeightLarge)
            .clickable { onClick(language) },
        shape = Shape.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HealthIconText(
                text = stringResource(
                    if (language == AppLanguage.VIETNAMESE) R.string.settings_icon_vietnamese
                    else R.string.settings_icon_english
                ),
                style = MaterialTheme.typography.titleLarge
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimens.spaceMedium)
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier.size(Dimens.selectionIndicatorSize),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    HealthIconText(
                        text = stringResource(R.string.settings_icon_check),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoDataCard(isLoading: Boolean, onClick: () -> Unit) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onClick),
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIconBubble(
                icon = stringResource(R.string.settings_icon_demo_data),
                isPrimary = true
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimens.spaceMedium)
            ) {
                Text(
                    text = stringResource(R.string.settings_demo_data_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimens.iconSizeMedium),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                HealthIconText(
                    text = stringResource(R.string.settings_icon_chevron),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun DangerZoneCard(isResetting: Boolean, onClick: () -> Unit) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isResetting, onClick = onClick),
        shape = Shape.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIconBubble(
                icon = stringResource(R.string.settings_icon_reset),
                isError = true
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimens.spaceMedium)
            ) {
                Text(
                    stringResource(R.string.settings_reset_data),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.settings_reset_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isResetting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimens.iconSizeMedium),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            } else {
                HealthIconText(
                    stringResource(R.string.settings_icon_chevron),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun SettingsIconBubble(
    icon: String,
    isPrimary: Boolean = false,
    isError: Boolean = false
) {
    val containerColor = when {
        isError -> MaterialTheme.colorScheme.error
        isPrimary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when {
        isError -> MaterialTheme.colorScheme.onError
        isPrimary -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    Box(
        modifier = Modifier
            .size(Dimens.buttonHeightMedium)
            .padding(Dimens.spaceExtraSmall),
        contentAlignment = Alignment.Center
    ) {
        HealthCard(
            modifier = Modifier.fillMaxSize(),
            shape = Shape.pill,
            colors = CardDefaults.cardColors(containerColor = containerColor)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                HealthIconText(text = icon, color = contentColor, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun DemoDataConfirmationDialog(
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(stringResource(R.string.settings_demo_data_confirm_title)) },
        text = { Text(stringResource(R.string.settings_demo_data_confirm_message)) },
        confirmButton = {
            HealthPrimaryButton(
                onClick = onConfirm,
                modifier = Modifier.width(Dimens.dialogActionWidth),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimens.iconSizeMedium),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = Dimens.borderWidthThick
                    )
                } else {
                    Text(stringResource(R.string.settings_demo_data_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(stringResource(R.string.settings_reset_cancel))
            }
        }
    )
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
                modifier = Modifier.width(Dimens.dialogActionWidth),
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
            TextButton(onClick = onDismiss, enabled = !isResetting) {
                Text(stringResource(R.string.settings_reset_cancel))
            }
        }
    )
}

@Preview
@Composable
private fun PreviewSettingsScreen() {
    HealthTrackerTheme {
        SettingsContent(
            themeType = AppThemeType.LIGHT,
            fontSize = AppFontSize.MEDIUM,
            reminderSettings = ReminderSettings(),
            hasExactAlarmAccess = true,
            selectedLanguage = AppLanguage.VIETNAMESE,
            isResetting = false,
            isLoadingDemoData = false,
            onThemeChanged = {},
            onFontSizeChanged = {},
            onLanguageChanged = {},
            onRemindersChanged = {},
            onReminderTimeChanged = { _, _ -> },
            onTestDinnerReminder = {},
            onRequestExactAlarmAccess = {},
            onNavigateToProfile = {},
            onNavigateBack = {},
            onLoadDemoData = {},
            onReset = {}
        )
    }
}
