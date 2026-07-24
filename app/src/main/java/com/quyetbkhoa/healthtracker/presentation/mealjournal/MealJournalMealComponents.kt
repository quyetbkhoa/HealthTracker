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
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthMarqueeText as Text
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
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.domain.model.FontScale
import com.quyetbkhoa.healthtracker.domain.model.ThemeMode
import com.quyetbkhoa.healthtracker.presentation.designsystem.Dimens
import com.quyetbkhoa.healthtracker.presentation.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.presentation.designsystem.MealTypeColorPalette
import com.quyetbkhoa.healthtracker.presentation.designsystem.Shape
import com.quyetbkhoa.healthtracker.presentation.designsystem.mealTypeColorPalette
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthIconText
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.healthColors
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
internal fun MealGroupCard(
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
internal fun MealGroupHeader(
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
internal fun MealRow(
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
internal fun EmptyMealRow() {
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
internal fun mealLabel(type: MealType): String = stringResource(
    when (type) {
        MealType.BREAKFAST -> R.string.add_meal_breakfast
        MealType.LUNCH -> R.string.add_meal_lunch
        MealType.DINNER -> R.string.add_meal_dinner
        MealType.SNACK -> R.string.add_meal_snack
    }
)

@Composable
internal fun mealIcon(type: MealType): String = stringResource(
    when (type) {
        MealType.BREAKFAST -> R.string.dashboard_icon_breakfast
        MealType.LUNCH -> R.string.dashboard_icon_lunch
        MealType.DINNER -> R.string.dashboard_icon_dinner
        MealType.SNACK -> R.string.dashboard_icon_snack
    }
)

internal fun formatNumber(value: Int): String = NumberFormat.getIntegerInstance().format(value)

internal fun formatDecimal(value: Double): String =
    NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 1
    }.format(value)

internal fun formatTime(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis)
    .atZone(ZoneId.systemDefault())
    .toLocalTime()
    .format(DateTimeFormatter.ofPattern("HH:mm"))

internal val previewMeals = listOf(
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
internal fun PreviewMealJournalLight() {
    HealthTrackerTheme(themeMode = ThemeMode.LIGHT, fontScale = FontScale.MEDIUM) {
        MealJournalScreen(
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
internal fun PreviewMealJournalDark() {
    HealthTrackerTheme(themeMode = ThemeMode.DARK, fontScale = FontScale.MEDIUM) {
        MealJournalScreen(
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
