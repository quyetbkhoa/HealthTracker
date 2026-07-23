package com.quyetbkhoa.healthtracker.presentation.activityhistory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthMarqueeText as Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthIconText
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.core.designsystem.healthColors
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun ActivityHistoryScreen(
    onNavigateBack: () -> Unit,
    onAddActivity: (Long) -> Unit,
    viewModel: ActivityHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ActivityHistoryContent(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onAddActivity = { onAddActivity(state.selectedEpochDay) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityHistoryContent(
    state: ActivityHistoryUiState,
    onAction: (ActivityHistoryAction) -> Unit,
    onNavigateBack: () -> Unit,
    onAddActivity: () -> Unit
) {
    var isDatePickerVisible by remember { mutableStateOf(false) }

    if (isDatePickerVisible) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.ofEpochDay(state.selectedEpochDay)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val day = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onAction(ActivityHistoryAction.SelectDay(day.toEpochDay()))
                        }
                        isDatePickerVisible = false
                    }
                ) {
                    Text(stringResource(R.string.meal_journal_date_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerVisible = false }) {
                    Text(stringResource(R.string.meal_journal_date_cancel))
                }
            }
        ) {
            DatePicker(pickerState)
        }
    }

    if (state.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { onAction(ActivityHistoryAction.DismissDelete) },
            title = { Text(stringResource(R.string.activity_history_delete_title)) },
            text = { Text(stringResource(R.string.activity_history_delete_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(ActivityHistoryAction.ConfirmDelete) }) {
                    Text(stringResource(R.string.activity_history_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ActivityHistoryAction.DismissDelete) }) {
                    Text(stringResource(R.string.meal_journal_date_cancel))
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (!state.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = onAddActivity,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.activity_history_add)) },
                    containerColor = MaterialTheme.healthColors.activity,
                    contentColor = MaterialTheme.healthColors.onActivity
                )
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.healthColors.activity)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Dimens.spaceMedium),
                contentPadding = PaddingValues(bottom = Dimens.buttonHeightLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
            ) {
                item {
                    ActivityHistoryHeader(
                        onNavigateBack = onNavigateBack,
                        onPickDate = { isDatePickerVisible = true }
                    )
                }
                item {
                    ActivityDateNavigator(
                        epochDay = state.selectedEpochDay,
                        onPrevious = { onAction(ActivityHistoryAction.PreviousDay) },
                        onNext = { onAction(ActivityHistoryAction.NextDay) },
                        onPickDate = { isDatePickerVisible = true }
                    )
                }
                item {
                    ActivityDailySummary(state)
                }
                item {
                    Text(
                        text = stringResource(R.string.activity_history_today_records),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (state.activities.isEmpty()) {
                    item { EmptyActivityHistory() }
                } else {
                    state.activities.forEach { activity ->
                        item(key = activity.id) {
                            ActivityHistoryCard(
                                activity = activity,
                                onDelete = {
                                    onAction(ActivityHistoryAction.RequestDelete(activity.id))
                                }
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(Dimens.spaceLarge)) }
            }
        }
    }
}

@Composable
private fun ActivityHistoryHeader(onNavigateBack: () -> Unit, onPickDate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.spaceMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.navigate_back))
        }
        Text(
            text = stringResource(R.string.activity_history_title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onPickDate) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(R.string.meal_journal_open_calendar),
                tint = MaterialTheme.healthColors.activity
            )
        }
    }
}

@Composable
private fun ActivityDateNavigator(
    epochDay: Long,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPickDate: () -> Unit
) {
    val selectedDate = LocalDate.ofEpochDay(epochDay)
    val label = if (selectedDate == LocalDate.now()) {
        stringResource(
            R.string.meal_journal_today_date,
            selectedDate.format(
                DateTimeFormatter.ofPattern(
                    stringResource(R.string.meal_journal_short_date_pattern)
                )
            )
        )
    } else {
        selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }

    HealthElevatedCard(modifier = Modifier.fillMaxWidth(), shape = Shape.large) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    stringResource(R.string.previous_day)
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Dimens.buttonHeightMedium)
                    .clickable(onClick = onPickDate),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.healthColors.activity
                )
                Text(
                    text = label,
                    modifier = Modifier.padding(start = Dimens.spaceSmall),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.healthColors.activity,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onNext) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    stringResource(R.string.next_day)
                )
            }
        }
    }
}

@Composable
private fun ActivityDailySummary(state: ActivityHistoryUiState) {
    HealthElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.healthColors.activityContainer,
            contentColor = MaterialTheme.healthColors.onActivityContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
        ) {
            HealthCard(
                modifier = Modifier.size(Dimens.buttonHeightLarge),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    HealthIconText(
                        text = stringResource(R.string.dashboard_icon_burned),
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
            ) {
                Text(
                    text = stringResource(R.string.activity_history_total_burned),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.activity_history_kcal,
                        formatNumber(state.totalCalories)
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.activity_history_summary,
                        state.activities.size,
                        state.totalDurationMinutes
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.healthColors.onActivityContainer.copy(alpha = 0.76f)
                )
            }
        }
    }
}

@Composable
private fun ActivityHistoryCard(
    activity: ActivityHistoryItem,
    onDelete: () -> Unit
) {
    val time = Instant.ofEpochMilli(activity.performedAt)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    HealthElevatedCard(modifier = Modifier.fillMaxWidth(), shape = Shape.extraLarge) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            HealthCard(
                modifier = Modifier.size(Dimens.buttonHeightMedium),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.healthColors.activityContainer,
                    contentColor = MaterialTheme.healthColors.onActivityContainer
                )
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    HealthIconText(text = activity.icon, style = MaterialTheme.typography.titleLarge)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.name.ifBlank {
                        stringResource(R.string.common_other)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.activity_history_record_detail,
                        activity.durationMinutes,
                        time
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(
                        R.string.activity_history_kcal,
                        formatNumber(activity.caloriesBurned)
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.healthColors.activity
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.activity_history_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyActivityHistory() {
    HealthElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.healthColors.activityContainer,
            contentColor = MaterialTheme.healthColors.onActivityContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            HealthIconText(
                text = stringResource(R.string.dashboard_icon_activity),
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = stringResource(R.string.activity_history_empty),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.healthColors.onActivityContainer
            )
        }
    }
}

private fun formatNumber(value: Int): String =
    NumberFormat.getIntegerInstance(Locale.getDefault()).format(value)

@Preview
@Composable
private fun PreviewActivityHistory() {
    HealthTrackerTheme {
        ActivityHistoryContent(
            state = ActivityHistoryUiState(
                isLoading = false,
                activities = listOf(
                    ActivityHistoryItem(1, "Đi bộ nhanh", "🚶", 30, 168, 1_753_177_400_000)
                )
            ),
            onAction = {},
            onNavigateBack = {},
            onAddActivity = {}
        )
    }
}
