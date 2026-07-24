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
internal fun DemoDataCard(isLoading: Boolean, onClick: () -> Unit) {
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
internal fun DangerZoneCard(isResetting: Boolean, onClick: () -> Unit) {
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
internal fun SettingsIconBubble(
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
internal fun DemoDataConfirmationDialog(
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
internal fun ResetConfirmationDialog(
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
internal fun PreviewSettingsScreen() {
    HealthTrackerTheme {
        SettingsContent(
            themeMode = ThemeMode.LIGHT,
            fontScale = FontScale.MEDIUM,
            reminderSettings = ReminderSettings(),
            hasExactAlarmAccess = true,
            selectedLanguage = AppLanguage.VIETNAMESE,
            isResetting = false,
            isLoadingDemoData = false,
            onThemeChanged = {},
            onFontScaleChanged = {},
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
