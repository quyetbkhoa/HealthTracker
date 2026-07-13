package com.quyetbkhoa.healthtracker.presentation.mealjournal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.MealType
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun MealJournalScreen(
    onNavigateBack: () -> Unit,
    onAddMeal: (Long, MealType) -> Unit,
    viewModel: MealJournalViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MealJournalContent(state, viewModel::onAction, onNavigateBack, onAddMeal)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealJournalContent(
    state: MealJournalUiState,
    onAction: (MealJournalAction) -> Unit,
    onNavigateBack: () -> Unit,
    onAddMeal: (Long, MealType) -> Unit
) {
    var isDatePickerVisible by remember { mutableStateOf(false) }
    if (isDatePickerVisible) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.ofEpochDay(state.selectedEpochDay)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val day = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onAction(MealJournalAction.SelectDay(day.toEpochDay()))
                    }
                    isDatePickerVisible = false
                }) { Text(stringResource(R.string.meal_journal_date_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerVisible = false }) {
                    Text(stringResource(R.string.meal_journal_date_cancel))
                }
            }
        ) { DatePicker(pickerState) }
    }
    if (state.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { onAction(MealJournalAction.DismissDelete) },
            title = { Text(stringResource(R.string.meal_journal_delete_title)) },
            text = { Text(stringResource(R.string.meal_journal_delete_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(MealJournalAction.ConfirmDelete) }) {
                    Text(stringResource(R.string.meal_journal_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(MealJournalAction.DismissDelete) }) {
                    Text(stringResource(R.string.meal_journal_date_cancel))
                }
            }
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
        ) {
            item { JournalHeader(onNavigateBack) }
            item {
                DateNavigator(
                    epochDay = state.selectedEpochDay,
                    onPrevious = { onAction(MealJournalAction.PreviousDay) },
                    onNext = { onAction(MealJournalAction.NextDay) },
                    onPickDate = { isDatePickerVisible = true }
                )
            }
            item { DailySummary(state.totalCalories, state.targetCalories) }
            MealType.entries.forEach { type ->
                item {
                    MealGroupHeader(
                        type = type,
                        calories = state.caloriesFor(type),
                        onAdd = { onAddMeal(state.selectedEpochDay, type) }
                    )
                }
                val typeMeals = state.mealsFor(type)
                if (typeMeals.isEmpty()) {
                    item(key = "empty_${type.name}") { EmptyMealRow() }
                } else {
                    items(typeMeals, key = MealEntry::id) { meal ->
                        MealRow(meal) { onAction(MealJournalAction.RequestDelete(meal.id)) }
                    }
                }
            }
            item {
                HealthPrimaryButton(
                    onClick = { onAddMeal(state.selectedEpochDay, MealType.BREAKFAST) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.buttonHeightMedium)
                ) {
                    Icon(Icons.Default.Add, null)
                    Text(stringResource(R.string.meal_journal_add_food))
                }
            }
            item { Spacer(Modifier.height(Dimens.spaceLarge)) }
        }
    }
}

@Composable
private fun JournalHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.spaceMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.navigate_back))
        }
        Text(
            stringResource(R.string.meal_journal_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DateNavigator(
    epochDay: Long,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPickDate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.previous_day))
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onPickDate),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = LocalDate.ofEpochDay(epochDay).format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    ),
                    modifier = Modifier.padding(start = Dimens.spaceSmall),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.next_day))
            }
        }
    }
}

@Composable
private fun DailySummary(total: Int, target: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(Dimens.spaceLarge)) {
            Text(stringResource(R.string.meal_journal_total_today))
            Text(
                stringResource(R.string.meal_journal_total_value, formatNumber(total), formatNumber(target)),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(R.string.meal_journal_remaining, formatNumber(target - total)),
                color = if (total > target) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MealGroupHeader(type: MealType, calories: Int, onAdd: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(mealIcon(type), style = MaterialTheme.typography.headlineSmall)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.spaceSmall)
        ) {
            Text(mealLabel(type), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.meal_journal_group_kcal, calories), color = MaterialTheme.colorScheme.primary)
        }
        TextButton(onClick = onAdd) {
            Icon(Icons.Default.Add, null)
            Text(stringResource(R.string.meal_journal_add_item))
        }
    }
}

@Composable
private fun MealRow(meal: MealEntry, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(meal.name, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(
                        R.string.meal_journal_item_detail,
                        formatDecimal(meal.consumedGrams),
                        formatTime(meal.eatenAt)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(stringResource(R.string.meal_journal_kcal, meal.calories), fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, stringResource(R.string.meal_journal_delete))
            }
        }
    }
}

@Composable
private fun EmptyMealRow() {
    Text(
        text = stringResource(R.string.meal_journal_empty_group),
        modifier = Modifier.padding(horizontal = Dimens.spaceMedium),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun mealLabel(type: MealType): String = stringResource(
    when (type) {
        MealType.BREAKFAST -> R.string.add_meal_breakfast
        MealType.LUNCH -> R.string.add_meal_lunch
        MealType.DINNER -> R.string.add_meal_dinner
        MealType.SNACK -> R.string.add_meal_snack
    }
)

@Composable
private fun mealIcon(type: MealType): String = stringResource(
    when (type) {
        MealType.BREAKFAST -> R.string.dashboard_icon_breakfast
        MealType.LUNCH -> R.string.dashboard_icon_lunch
        MealType.DINNER -> R.string.dashboard_icon_dinner
        MealType.SNACK -> R.string.dashboard_icon_snack
    }
)

private fun formatNumber(value: Int): String = NumberFormat.getIntegerInstance().format(value)
private fun formatDecimal(value: Double): String =
    NumberFormat.getNumberInstance(Locale.getDefault()).apply { maximumFractionDigits = 1 }.format(value)
private fun formatTime(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis)
    .atZone(ZoneId.systemDefault())
    .toLocalTime()
    .format(DateTimeFormatter.ofPattern("HH:mm"))
