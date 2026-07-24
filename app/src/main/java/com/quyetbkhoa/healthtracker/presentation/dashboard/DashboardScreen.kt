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
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthMarqueeText as Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.healthColors
import com.quyetbkhoa.healthtracker.core.designsystem.mealTypeColorPalette
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthIconText
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
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToAddMeal: () -> Unit,
    onNavigateToAddActivity: () -> Unit,
    onNavigateToActivityHistory: () -> Unit,
    onNavigateToMealJournal: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                DashboardUiEvent.NavigateToAddMeal -> onNavigateToAddMeal()
                DashboardUiEvent.NavigateToAddActivity -> onNavigateToAddActivity()
                DashboardUiEvent.NavigateToMealJournal -> onNavigateToMealJournal()
            }
        }
    }
    DashboardContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToActivityHistory = onNavigateToActivityHistory,
        onNavigateToStatistics = onNavigateToStatistics
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToActivityHistory: () -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    if (uiState.isLoading || !uiState.hasProfile) {
        DashboardLoadingState(isLoading = uiState.isLoading)
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DashboardBottomBar(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToMeals = { onAction(DashboardAction.ViewMeals) },
                onNavigateToActivity = onNavigateToActivityHistory,
                onNavigateToStatistics = onNavigateToStatistics
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            DashboardHeader(userName = uiState.userName)
            CalorieOverviewCard(uiState = uiState)
            uiState.suggestedActivityLevel?.let {
                ActivityLevelSuggestionCard(uiState, onNavigateToSettings)
            }
            CalorieAdviceCard(uiState = uiState)
            DashboardQuickActions(onAction = onAction)
            TodayMealsSection(uiState = uiState, onAction = onAction)
            Spacer(Modifier.height(Dimens.spaceSmall))

        }
    }
}

@Composable
private fun ActivityLevelSuggestionCard(uiState: DashboardUiState, onOpenSettings: () -> Unit) {
    val level = uiState.suggestedActivityLevel ?: return
    val levelName = stringResource(
        when (level) {
            com.quyetbkhoa.healthtracker.domain.model.ActivityLevel.SEDENTARY -> R.string.onboarding_activity_short_sedentary
            com.quyetbkhoa.healthtracker.domain.model.ActivityLevel.LIGHT -> R.string.onboarding_activity_short_light
            com.quyetbkhoa.healthtracker.domain.model.ActivityLevel.MODERATE -> R.string.onboarding_activity_short_moderate
            com.quyetbkhoa.healthtracker.domain.model.ActivityLevel.VERY_ACTIVE -> R.string.onboarding_activity_short_active
            com.quyetbkhoa.healthtracker.domain.model.ActivityLevel.EXTRA_ACTIVE -> R.string.onboarding_activity_short_extra_active
        }
    )
    DashboardCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenSettings)) {
        Column(modifier = Modifier.padding(Dimens.spaceMedium), verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
            Text(stringResource(R.string.dashboard_activity_suggestion_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.dashboard_activity_suggestion_message, levelName, formatNumber(uiState.suggestedTdeeCalories)),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardLoadingState(isLoading: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(if (isLoading) R.string.dashboard_loading else R.string.dashboard_no_profile),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun DashboardHeader(userName: String) {
    val locale = LocalConfiguration.current.locales[0]
    val formattedDate = LocalDate.now()
        .format(DateTimeFormatter.ofPattern(stringResource(R.string.dashboard_date_pattern), locale))
        .replaceFirstChar { it.titlecase(locale) }
    val displayName = userName.substringBeforeLast(' ').ifBlank { stringResource(R.string.dashboard_guest_name) }
    val greeting = buildAnnotatedString {
        append(stringResource(R.string.dashboard_greeting, ""))
        withStyle(SpanStyle(color = MaterialTheme.healthColors.meal)) {
            append(displayName)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = greeting,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            marquee = true
        )
        DashboardBrandMark()
        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
        Text(
            text = stringResource(R.string.dashboard_date_with_icon, formattedDate),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.healthColors.meal,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            marquee = true
        )
    }
}

/** Reserved visual slot for the dashboard brand asset. */
@Composable
private fun DashboardBrandMark() {
    Box(
        modifier = Modifier.size(Dimens.iconSizeLarge),
        contentAlignment = Alignment.Center
    ) {
        HealthIconText(
            text = stringResource(R.string.dashboard_brand_mark),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun CalorieOverviewCard(uiState: DashboardUiState) {
    val isExceeded = uiState.isExceeded
    val containerColor = if (isExceeded) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.healthColors.mealContainer
    }
    val contentColor = if (isExceeded) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.healthColors.onMealContainer
    }
    HealthElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = DashboardDimens.overviewHeight)
                .padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoalRing(
                targetCalories = uiState.targetCalories,
                progress = uiState.progress,
                isExceeded = isExceeded,
                contentColor = contentColor,
                modifier = Modifier.size(DashboardDimens.goalRingSize)
            )
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
            ) {
                OverviewMetric(
                    R.string.dashboard_icon_eaten,
                    R.string.dashboard_consumed,
                    uiState.consumedCalories,
                    contentColor = contentColor
                )
                DashboardSubtleDivider()
                OverviewMetric(
                    R.string.dashboard_icon_burned,
                    R.string.dashboard_burned,
                    uiState.exerciseCalories,
                    contentColor = contentColor
                )
                DashboardSubtleDivider()
                OverviewMetric(
                    R.string.dashboard_icon_remaining,
                    if (isExceeded) R.string.dashboard_exceeded else R.string.dashboard_remaining,
                    abs(uiState.remainingCalories),
                    contentColor = contentColor
                )
            }
        }
    }
}

@Composable
private fun DashboardSubtleDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun GoalRing(
    targetCalories: Int,
    progress: Float,
    isExceeded: Boolean,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val progressColor = if (isExceeded) scheme.error else MaterialTheme.healthColors.meal
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = DashboardDimens.progressStroke.toPx()
            drawCircle(
                color = contentColor.copy(alpha = 0.16f),
                style = Stroke(stroke),
                radius = (size.minDimension-stroke)/2f
                )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = progress.coerceIn(0f, 1f) * 360f,
                useCenter = false,
                topLeft = Offset(
                    x = stroke/2f,
                    y = stroke/2f
                ),
                size = Size(size.width-stroke , size.height-stroke ),
                style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.dashboard_goal), style = MaterialTheme.typography.labelLarge, color = contentColor)
            Text(text = formatNumber(targetCalories), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = contentColor)

        }
    }
}

@Composable
private fun OverviewMetric(
    iconRes: Int,
    labelRes: Int,
    value: Int,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(labelRes),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.78f),
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatNumber(value),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
            HealthIconText(text = stringResource(iconRes), style = MaterialTheme.typography.displaySmall, color = contentColor)

    }
}

@Composable
private fun CalorieAdviceCard(uiState: DashboardUiState) {
    val evaluation = uiState.calorieEvaluation
    var isInfoVisible by remember { mutableStateOf(false) }
    val isOnTarget = evaluation.status == DailyCalorieStatus.GOOD
    val containerColor = if (isOnTarget) {
        MaterialTheme.healthColors.mealContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = if (isOnTarget) {
        MaterialTheme.healthColors.onMealContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    val advice = when (evaluation.status) {
        DailyCalorieStatus.NEEDS_MORE -> DashboardAdvice(
            title = stringResource(R.string.dashboard_advice_need_more_title),
            detail = stringResource(
                R.string.dashboard_advice_short_need_more_detail,
                formatNumber(evaluation.caloriesToBoundary)
            )
        )
        DailyCalorieStatus.GOOD -> DashboardAdvice(
            title = stringResource(R.string.dashboard_advice_good_title),
            detail = stringResource(R.string.dashboard_advice_short_good_detail)
        )
        DailyCalorieStatus.EXCEEDED -> DashboardAdvice(
            title = stringResource(R.string.dashboard_advice_exceeded_title),
            detail = stringResource(
                R.string.dashboard_advice_short_exceeded_detail,
                formatNumber(evaluation.caloriesToBoundary)
            )
        )
    }
    HealthElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.extraLarge,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.spaceMedium, top = Dimens.spaceSmall, bottom = Dimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HealthIconText(
                text = stringResource(if (isOnTarget) R.string.dashboard_mood_happy else R.string.dashboard_mood_sad),
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)) {
                Text(
                    text = advice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = advice.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.78f)
                )
            }
            IconButton(onClick = { isInfoVisible = true }) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.dashboard_target_range_info),
                    tint = contentColor
                )
            }
        }
    }

    if (isInfoVisible) {
        AlertDialog(
            onDismissRequest = { isInfoVisible = false },
            icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
            title = { Text(stringResource(R.string.dashboard_target_range_info_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.dashboard_target_range_info_message,
                        formatNumber(evaluation.lowerBound),
                        formatNumber(evaluation.upperBound),
                        formatNumber(evaluation.upperBound)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { isInfoVisible = false }) {
                    Text(stringResource(R.string.dashboard_target_range_info_confirm))
                }
            }
        )
    }
}

private data class DashboardAdvice(
    val title: String,
    val detail: String
)

@Composable
private fun DashboardQuickActions(onAction: (DashboardAction) -> Unit) {
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
private fun QuickActionCard(
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
private fun TodayMealsSection(uiState: DashboardUiState, onAction: (DashboardAction) -> Unit) {
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
private fun MealCard(
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
private fun MealDetailsDialog(
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

@Composable
private fun DashboardBottomBar(
    onNavigateToSettings: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = Dimens.spaceLarge, topEnd = Dimens.spaceLarge),
        shadowElevation = Dimens.spaceSmall
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.spaceSmall),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationItem(R.string.dashboard_icon_home, R.string.dashboard_home, true, {})
            NavigationItem(R.string.dashboard_icon_meals_nav, R.string.dashboard_meals, false, onNavigateToMeals)
            NavigationItem(
                R.string.dashboard_icon_activity_nav,
                R.string.dashboard_activity,
                false,
                onNavigateToActivity
            )
            NavigationItem(R.string.dashboard_icon_statistics, R.string.dashboard_statistics, false, onNavigateToStatistics)
            NavigationItem(R.string.dashboard_icon_settings, R.string.dashboard_settings, false, onNavigateToSettings)
        }
    }
}

@Composable
private fun RowScope.NavigationItem(iconRes: Int, labelRes: Int, selected: Boolean, onClick: () -> Unit) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val density = LocalDensity.current
    val screenWidthDp = with(density) {
        LocalWindowInfo.current.containerSize.width.toDp().value
    }
    val fontScale = density.fontScale
    val labelStyle = if (screenWidthDp < COMPACT_NAVIGATION_WIDTH_DP || fontScale > LARGE_FONT_SCALE) {
        MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp)
    } else {
        MaterialTheme.typography.labelMedium
    }
    Column(
        modifier = Modifier.weight(1f).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
    ) {
        HealthIconText(text = stringResource(iconRes), style = MaterialTheme.typography.headlineSmall, color = contentColor)
        Text(
            text = stringResource(labelRes),
            modifier = Modifier.fillMaxWidth(),
            style = labelStyle,
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .width(Dimens.progressBarWidth)
                .height(Dimens.progressBarHeight)
                .clip(CircleShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                )
        )
    }
}

@Composable
private fun DashboardCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    HealthElevatedCard(
        modifier = modifier,
        shape = Shape.extraLarge,
        content = { content() }
    )
}

private fun formatNumber(value: Int): String = NumberFormat.getIntegerInstance(Locale.getDefault()).format(value)

private const val COMPACT_NAVIGATION_WIDTH_DP = 360
private const val LARGE_FONT_SCALE = 1.15f


@Composable
@Preview
private fun PreviewDashboardScreen() {
    HealthTrackerTheme {
        DashboardContent(
            uiState = DashboardUiState(
                isLoading = false,
                hasProfile = true,
                targetCalories = 2_000,
                consumedCalories = 1_900,
                calorieEvaluation = DailyCalorieEvaluation(
                    status = DailyCalorieStatus.GOOD,
                    lowerBound = 1_800,
                    upperBound = 2_200,
                    caloriesToBoundary = 0
                ),
                userName = "Nguyễn An"
            ),
            onAction = {},
            onNavigateToSettings = {},
            onNavigateToActivityHistory = {},
            onNavigateToStatistics = {}
        )
    }
}
