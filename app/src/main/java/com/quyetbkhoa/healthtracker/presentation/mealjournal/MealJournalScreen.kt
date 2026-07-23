package com.quyetbkhoa.healthtracker.presentation.mealjournal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.domain.model.FontScale
import com.quyetbkhoa.healthtracker.domain.model.ThemeMode
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.MealTypeColorPalette
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.mealTypeColorPalette
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthIconText
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.core.designsystem.healthColors
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieEvaluation
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieStatus
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
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
        val pickerState = rememberDatePickerState(
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
                }) {
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (!state.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = { onAddMeal(state.selectedEpochDay, MealType.BREAKFAST) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.meal_journal_add_food)) },
                    containerColor = MaterialTheme.healthColors.meal,
                    contentColor = MaterialTheme.healthColors.onMeal
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
                CircularProgressIndicator(color = MaterialTheme.healthColors.meal)
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
                    JournalHeader(
                        onNavigateBack = onNavigateBack,
                        onPickDate = { isDatePickerVisible = true }
                    )
                }
                item {
                    DateNavigator(
                        epochDay = state.selectedEpochDay,
                        onPrevious = { onAction(MealJournalAction.PreviousDay) },
                        onNext = { onAction(MealJournalAction.NextDay) },
                        onPickDate = { isDatePickerVisible = true }
                    )
                }
                item {
                    DailySummary(
                        total = state.totalCalories,
                        target = state.targetCalories,
                        evaluation = state.calorieEvaluation
                    )
                }
                MealType.entries.forEach { type ->
                    item(key = "meal-group-${type.name}") {
                        MealGroupCard(
                            type = type,
                            calories = state.caloriesFor(type),
                            meals = state.mealsFor(type),
                            onAdd = { onAddMeal(state.selectedEpochDay, type) },
                            onDelete = { mealId ->
                                onAction(MealJournalAction.RequestDelete(mealId))
                            }
                        )
                    }
                }
                item { Spacer(Modifier.height(Dimens.spaceLarge)) }
            }
        }
    }
}

@Composable
private fun JournalHeader(onNavigateBack: () -> Unit, onPickDate: () -> Unit) {
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
            text = stringResource(R.string.meal_journal_title),
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
                tint = MaterialTheme.healthColors.meal
            )
        }
    }
}

@Composable
private fun DateNavigator(
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
                DateTimeFormatter.ofPattern(stringResource(R.string.meal_journal_short_date_pattern))
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
                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.healthColors.meal)
                Text(
                    text = label,
                    modifier = Modifier.padding(start = Dimens.spaceSmall),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.healthColors.meal,
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
private fun DailySummary(
    total: Int,
    target: Int,
    evaluation: DailyCalorieEvaluation
) {
    val progress = if (target > 0) (total.toFloat() / target).coerceIn(0f, 1f) else 0f
    val percent = if (target > 0) ((total.toLong() * 100) / target).toInt().coerceAtLeast(0) else 0
    val isGoalWarning = target > 0 && evaluation.status != DailyCalorieStatus.GOOD
    val balanceText = if (evaluation.status == DailyCalorieStatus.EXCEEDED) {
        stringResource(R.string.meal_journal_exceeded, formatNumber(total - target))
    } else {
        stringResource(R.string.meal_journal_remaining, formatNumber(target - total))
    }

    HealthElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (isGoalWarning) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.healthColors.mealContainer,
            contentColor = if (isGoalWarning) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.healthColors.onMealContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceLarge)
        ) {
            SummaryProgressRing(
                progress = progress,
                percent = percent,
                isGoalWarning = isGoalWarning
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
            ) {
                Text(
                    text = stringResource(R.string.meal_journal_total_today),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.meal_journal_total_value,
                        formatNumber(total),
                        formatNumber(target)
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isGoalWarning) MaterialTheme.colorScheme.error
                    else MaterialTheme.healthColors.onMealContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = balanceText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isGoalWarning) MaterialTheme.colorScheme.error
                    else MaterialTheme.healthColors.onMealContainer
                )
            }
        }
    }
}

@Composable
private fun SummaryProgressRing(progress: Float, percent: Int, isGoalWarning: Boolean) {
    val progressColor = if (isGoalWarning) MaterialTheme.colorScheme.error
    else MaterialTheme.healthColors.meal
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = if (isGoalWarning) MaterialTheme.colorScheme.error
    else MaterialTheme.healthColors.onMealContainer
    val accessibilityLabel = stringResource(R.string.meal_journal_used_percent, percent)

    Box(
        modifier = Modifier.size(MealJournalDimens.summaryRingSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = accessibilityLabel }
        ) {
            val strokeWidth = MealJournalDimens.summaryRingStroke.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            if (progress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = Size(
                        width = size.width - strokeWidth,
                        height = size.height - strokeWidth
                    ),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.statistics_percent, percent),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = stringResource(R.string.meal_journal_used),
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

@Composable
private fun MealGroupCard(
    type: MealType,
    calories: Int,
    meals: List<MealEntry>,
    onAdd: () -> Unit,
    onDelete: (Long) -> Unit
) {
    val colors = mealTypeColorPalette(type)

    HealthElevatedCard(modifier = Modifier.fillMaxWidth(), shape = Shape.extraLarge) {
        MealGroupHeader(type = type, calories = calories, colors = colors, onAdd = onAdd)
        if (meals.isEmpty()) {
            EmptyMealRow()
        } else {
            meals.forEachIndexed { index, meal ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Dimens.spaceMedium),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                MealRow(meal = meal, accentColor = colors.accent) { onDelete(meal.id) }
            }
        }
    }
}

@Composable
private fun MealGroupHeader(
    type: MealType,
    calories: Int,
    colors: MealTypeColorPalette,
    onAdd: () -> Unit
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.spaceSmall),
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = colors.container,
            contentColor = colors.content
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            HealthCard(
                modifier = Modifier.size(MealJournalDimens.mealIconContainerSize),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    HealthIconText(
                        text = mealIcon(type),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mealLabel(type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.meal_journal_group_kcal, calories),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.content
                )
            }
            OutlinedButton(
                onClick = onAdd,
                modifier = Modifier.heightIn(min = Dimens.buttonHeightMedium),
                border = BorderStroke(Dimens.borderWidthThin, colors.accent),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.content),
                contentPadding = PaddingValues(horizontal = Dimens.spaceSmall)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(Dimens.spaceExtraSmall))
                Text(
                    text = stringResource(R.string.meal_journal_add_item),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MealRow(
    meal: MealEntry,
    accentColor: Color,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        HealthIconText(
            text = stringResource(R.string.add_meal_food_icon),
            style = MaterialTheme.typography.titleLarge
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = meal.name,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    R.string.meal_journal_item_detail,
                    formatDecimal(meal.consumedGrams),
                    formatTime(meal.eatenAt)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = stringResource(R.string.meal_journal_kcal, meal.calories),
            fontWeight = FontWeight.Bold,
            color = accentColor,
            maxLines = 1
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.meal_journal_delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyMealRow() {
    Text(
        text = stringResource(R.string.meal_journal_empty_group),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceLarge),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
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
    NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 1
    }.format(value)

private fun formatTime(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis)
    .atZone(ZoneId.systemDefault())
    .toLocalTime()
    .format(DateTimeFormatter.ofPattern("HH:mm"))

private val previewMeals = listOf(
    MealEntry(
        id = 1L,
        foodId = 1L,
        name = "Cơm trắng",
        calories = 260,
        mealType = MealType.BREAKFAST,
        consumedGrams = 200.0,
        caloriesPer100GramsSnapshot = 130.0,
        eatenAt = System.currentTimeMillis()
    ),
    MealEntry(
        id = 2L,
        foodId = 2L,
        name = "Phở bò",
        calories = 444,
        mealType = MealType.LUNCH,
        consumedGrams = 400.0,
        caloriesPer100GramsSnapshot = 111.0,
        eatenAt = System.currentTimeMillis()
    )
)

@Preview
@Composable
private fun PreviewMealJournalLight() {
    HealthTrackerTheme(themeMode = ThemeMode.LIGHT, fontScale = FontScale.MEDIUM) {
        MealJournalContent(
            state = MealJournalUiState(
                isLoading = false,
                targetCalories = 2_000,
                meals = previewMeals
            ),
            onAction = {},
            onNavigateBack = {},
            onAddMeal = { _, _ -> }
        )
    }
}

@Preview
@Composable
private fun PreviewMealJournalDark() {
    HealthTrackerTheme(themeMode = ThemeMode.DARK, fontScale = FontScale.MEDIUM) {
        MealJournalContent(
            state = MealJournalUiState(
                isLoading = false,
                targetCalories = 2_000,
                meals = previewMeals
            ),
            onAction = {},
            onNavigateBack = {},
            onAddMeal = { _, _ -> }
        )
    }
}
