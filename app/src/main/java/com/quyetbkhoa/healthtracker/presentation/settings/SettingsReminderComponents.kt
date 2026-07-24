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
internal fun ReminderSettingsCard(
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
internal fun ReminderTimeRow(
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

