package com.quyetbkhoa.healthtracker.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthMarqueeText as Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.presentation.designsystem.Dimens
import com.quyetbkhoa.healthtracker.presentation.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.presentation.designsystem.Shape
import com.quyetbkhoa.healthtracker.presentation.designsystem.healthColors
import com.quyetbkhoa.healthtracker.presentation.designsystem.mealTypeColorPalette
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthIconText
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieStatus
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieEvaluation
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
internal fun DashboardQuickActions(onAction: (DashboardAction) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
        QuickActionCard(
            iconRes = R.string.dashboard_icon_meal,
            titleRes = R.string.dashboard_add_meal,
            isMeal = true,
            onClick = { onAction(DashboardAction.AddMeal) },
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            iconRes = R.string.dashboard_icon_activity,
            titleRes = R.string.dashboard_add_activity,
            isMeal = false,
            onClick = { onAction(DashboardAction.AddActivity) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun QuickActionCard(
    iconRes: Int,
    titleRes: Int,
    isMeal: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val containerColor = if (isMeal) {
        MaterialTheme.healthColors.meal
    } else {
        MaterialTheme.healthColors.activity
    }
    val contentColor = if (isMeal) {
        MaterialTheme.healthColors.onMeal
    } else {
        MaterialTheme.healthColors.onActivity
    }
    HealthElevatedCard(
        modifier = modifier
            .heightIn(min = Dimens.buttonHeightLarge)
            .clickable(onClick = onClick),
        shape = Shape.extraLarge,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(Dimens.buttonHeightMedium), shadowElevation = Dimens.spaceExtraSmall) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    HealthIconText(text = stringResource(iconRes), style = MaterialTheme.typography.headlineSmall)
                }
            }
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
            Text(
                text = stringResource(titleRes),
            style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun TodayMealsSection(uiState: DashboardUiState, onAction: (DashboardAction) -> Unit) {
    var selectedMealType by remember { mutableStateOf<MealType?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.dashboard_meals_today), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(
                text = stringResource(R.string.dashboard_view_all),
                modifier = Modifier.clickable { onAction(DashboardAction.ViewMeals) },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.healthColors.meal
            )
            HealthIconText(text = stringResource(R.string.dashboard_icon_arrow), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.healthColors.meal)
        }
        val mealCards = listOf(
            Triple(R.string.dashboard_icon_breakfast, R.string.dashboard_breakfast, MealType.BREAKFAST),
            Triple(R.string.dashboard_icon_lunch, R.string.dashboard_lunch, MealType.LUNCH),
            Triple(R.string.dashboard_icon_dinner, R.string.dashboard_dinner, MealType.DINNER),
            Triple(R.string.dashboard_icon_snack, R.string.dashboard_snack, MealType.SNACK)
        )
        mealCards.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
            ) {
                rowItems.forEach { (iconRes, titleRes, mealType) ->
                    MealCard(
                        iconRes = iconRes,
                        titleRes = titleRes,
                        mealType = mealType,
                        meals = uiState.meals.filter { it.mealType == mealType },
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMealType = mealType }
                    )
                }
            }
        }
    }
    selectedMealType?.let { mealType ->
        MealDetailsDialog(
            mealType = mealType,
            meals = uiState.meals.filter { it.mealType == mealType },
            onDismiss = { selectedMealType = null }
        )
    }
}

@Composable
internal fun MealCard(
    iconRes: Int,
    titleRes: Int,
    mealType: MealType,
    meals: List<MealEntry>,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val calories = meals.sumOf(MealEntry::calories)
    val colors = mealTypeColorPalette(mealType)
    HealthElevatedCard(
        modifier = modifier
            .heightIn(min = DashboardDimens.mealCardHeight)
            .clickable(onClick = onClick),
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = colors.container,
            contentColor = colors.content
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            Box(modifier = Modifier.size(Dimens.buttonHeightMedium).clip(CircleShape).background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                HealthIconText(text = stringResource(iconRes), style = MaterialTheme.typography.titleLarge)
            }
            Text(
                text = stringResource(titleRes),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.content,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.dashboard_meal_records, meals.size),
                style = MaterialTheme.typography.labelLarge,
                color = colors.content.copy(alpha = 0.76f)
            )
            Text(
                text = stringResource(R.string.dashboard_meal_kcal, calories),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.content,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun MealDetailsDialog(
    mealType: MealType,
    meals: List<MealEntry>,
    onDismiss: () -> Unit
) {
    val colors = mealTypeColorPalette(mealType)
    val title = stringResource(
        when (mealType) {
            MealType.BREAKFAST -> R.string.dashboard_breakfast
            MealType.LUNCH -> R.string.dashboard_lunch
            MealType.DINNER -> R.string.dashboard_dinner
            MealType.SNACK -> R.string.dashboard_snack
        }
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            HealthIconText(
                text = stringResource(
                    when (mealType) {
                        MealType.BREAKFAST -> R.string.dashboard_icon_breakfast
                        MealType.LUNCH -> R.string.dashboard_icon_lunch
                        MealType.DINNER -> R.string.dashboard_icon_dinner
                        MealType.SNACK -> R.string.dashboard_icon_snack
                    }
                ),
                style = MaterialTheme.typography.headlineLarge
            )
        },
        title = {
            Text(
                text = stringResource(R.string.dashboard_meal_dialog_title, title),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = DashboardDimens.mealDialogMaxHeight)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
            ) {
                if (meals.isEmpty()) {
                    Text(
                        text = stringResource(R.string.dashboard_meal_dialog_empty),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.spaceMedium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    meals.forEachIndexed { index, meal ->
                        if (index > 0) DashboardSubtleDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.spaceExtraSmall),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
                        ) {
                            Text(
                                text = meal.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.dashboard_meal_kcal, meal.calories),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent
                            )
                        }
                    }
                    DashboardSubtleDivider()
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.dashboard_meal_dialog_total),
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(
                                R.string.dashboard_meal_kcal,
                                meals.sumOf(MealEntry::calories)
                            ),
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dashboard_meal_dialog_close))
            }
        }
    )
}
