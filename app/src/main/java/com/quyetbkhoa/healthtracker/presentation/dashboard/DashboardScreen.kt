package com.quyetbkhoa.healthtracker.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
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
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieStatus
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
        onNavigateToStatistics = onNavigateToStatistics
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    onNavigateToSettings: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceLarge)
        ) {
            DashboardHeader(userName = uiState.userName, onNavigateToSettings = onNavigateToSettings)
            CalorieOverviewCard(uiState = uiState)
            uiState.suggestedActivityLevel?.let {
                ActivityLevelSuggestionCard(uiState, onNavigateToSettings)
            }
            CalorieAdviceCard(uiState = uiState)
            DashboardQuickActions(onAction = onAction)
            TodayMealsSection(uiState = uiState, onAction = onAction)
            DailyTipCard()
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
private fun DashboardHeader(userName: String, onNavigateToSettings: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val formattedDate = LocalDate.now()
        .format(DateTimeFormatter.ofPattern(stringResource(R.string.dashboard_date_pattern), locale))
        .replaceFirstChar { it.titlecase(locale) }
    val displayName = userName.substringBeforeLast(' ').ifBlank { stringResource(R.string.dashboard_guest_name) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
//            .statusBarsPadding()
            ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
            Text(
                text = stringResource(R.string.dashboard_greeting, displayName),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.dashboard_date_with_icon, formattedDate),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        NotificationButton(onClick = onNavigateToSettings)
    }
}

@Composable
private fun NotificationButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(Dimens.buttonHeightMedium)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.dashboard_icon_notification), style = MaterialTheme.typography.headlineSmall)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(Dimens.iconSizeLarge)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.dashboard_notification_count),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun CalorieOverviewCard(uiState: DashboardUiState) {
    DashboardCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(DashboardDimens.overviewHeight)
                .padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoalRing(
                targetCalories = uiState.targetCalories,
                progress = uiState.progress,
                modifier = Modifier.weight(1f).aspectRatio(1f)
            )
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
            Column(modifier = Modifier.weight(1.55f)) {
                Row {
                    OverviewMetric(R.string.dashboard_icon_eaten, R.string.dashboard_consumed, uiState.consumedCalories, Modifier.weight(1f))
                    OverviewMetric(R.string.dashboard_icon_burned, R.string.dashboard_burned, uiState.exerciseCalories, Modifier.weight(1f))
                    OverviewMetric(R.string.dashboard_icon_remaining, R.string.dashboard_remaining, abs(uiState.remainingCalories), Modifier.weight(1f), uiState.isExceeded)
                }
            }
        }
    }
}

@Composable
private fun GoalRing(targetCalories: Int, progress: Float, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = DashboardDimens.progressStroke.toPx()
            drawCircle(
                color = scheme.surfaceContainerHigh,
                style = Stroke(stroke),
                radius = (size.minDimension-stroke)/2f
                )
            drawArc(
                color = scheme.primary,
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
            Text(text = stringResource(R.string.dashboard_goal), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
            Text(text = formatNumber(targetCalories), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = scheme.onSurface)

        }
    }
}

@Composable
private fun OverviewMetric(iconRes: Int, labelRes: Int, value: Int, modifier: Modifier, isExceeded: Boolean = false) {
    val valueColor = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(Dimens.buttonHeightMedium)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(iconRes), style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(Dimens.spaceSmall))
        Text(text = stringResource(labelRes),
            modifier = Modifier.basicMarquee(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            )
        Text(text = formatNumber(value), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun ProgressLine(progress: Float, progressPercent: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.progressBarHeight)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(Dimens.progressBarHeight)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Text(
            text = stringResource(R.string.dashboard_percent_goal, progressPercent),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun CalorieAdviceCard(uiState: DashboardUiState) {
    val evaluation = uiState.calorieEvaluation
    val title = when (evaluation.status) {
        DailyCalorieStatus.NEEDS_MORE -> stringResource(R.string.dashboard_advice_need_more_title)
        DailyCalorieStatus.GOOD -> stringResource(R.string.dashboard_advice_good_title)
        DailyCalorieStatus.EXCEEDED -> stringResource(R.string.dashboard_advice_exceeded_title)
    }
    val message = when (evaluation.status) {
        DailyCalorieStatus.NEEDS_MORE -> when (uiState.goal) {
            Goal.LOSE_WEIGHT -> stringResource(R.string.dashboard_advice_lose_need_more, formatNumber(evaluation.caloriesToBoundary))
            Goal.MAINTAIN -> stringResource(R.string.dashboard_advice_maintain_need_more, formatNumber(evaluation.caloriesToBoundary))
            Goal.GAIN_WEIGHT -> stringResource(R.string.dashboard_advice_gain_need_more, formatNumber(evaluation.caloriesToBoundary))
        }
        DailyCalorieStatus.GOOD -> when (uiState.goal) {
            Goal.LOSE_WEIGHT -> stringResource(R.string.dashboard_advice_lose_good)
            Goal.MAINTAIN -> stringResource(R.string.dashboard_advice_maintain_good)
            Goal.GAIN_WEIGHT -> stringResource(R.string.dashboard_advice_gain_good)
        }
        DailyCalorieStatus.EXCEEDED -> when (uiState.goal) {
            Goal.LOSE_WEIGHT -> stringResource(R.string.dashboard_advice_lose_exceeded, formatNumber(evaluation.caloriesToBoundary))
            Goal.MAINTAIN -> stringResource(R.string.dashboard_advice_maintain_exceeded, formatNumber(evaluation.caloriesToBoundary))
            Goal.GAIN_WEIGHT -> stringResource(R.string.dashboard_advice_gain_exceeded, formatNumber(evaluation.caloriesToBoundary))
        }
    }
    DashboardCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(Dimens.spaceMedium), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(Dimens.buttonHeightLarge)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.dashboard_icon_star), style = MaterialTheme.typography.displaySmall)
            }
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DashboardQuickActions(onAction: (DashboardAction) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
        QuickActionCard(
            iconRes = R.string.dashboard_icon_meal,
            titleRes = R.string.dashboard_add_meal,
            isPrimary = true,
            onClick = { onAction(DashboardAction.AddMeal) },
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            iconRes = R.string.dashboard_icon_activity,
            titleRes = R.string.dashboard_add_activity,
            isPrimary = false,
            onClick = { onAction(DashboardAction.AddActivity) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(iconRes: Int, titleRes: Int, isPrimary: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val containerColor = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    HealthElevatedCard(
        modifier = modifier.height(Dimens.buttonHeightLarge).clickable(onClick = onClick),
        shape = Shape.extraLarge,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(Dimens.spaceMedium), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(Dimens.buttonHeightMedium), shadowElevation = Dimens.spaceExtraSmall) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text(text = stringResource(iconRes), style = MaterialTheme.typography.headlineSmall) }
            }
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
            Text(text = stringResource(titleRes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
private fun TodayMealsSection(uiState: DashboardUiState, onAction: (DashboardAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.dashboard_meals_today), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(
                text = stringResource(R.string.dashboard_view_all),
                modifier = Modifier.clickable { onAction(DashboardAction.ViewMeals) },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = stringResource(R.string.dashboard_icon_arrow), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
            MealCard(R.string.dashboard_icon_breakfast, R.string.dashboard_breakfast, uiState.meals.filter { it.mealType == MealType.BREAKFAST }, Modifier.weight(1f))
            MealCard(R.string.dashboard_icon_lunch, R.string.dashboard_lunch, uiState.meals.filter { it.mealType == MealType.LUNCH }, Modifier.weight(1f))
            MealCard(R.string.dashboard_icon_dinner, R.string.dashboard_dinner, uiState.meals.filter { it.mealType == MealType.DINNER }, Modifier.weight(1f))
            MealCard(R.string.dashboard_icon_snack, R.string.dashboard_snack, uiState.meals.filter { it.mealType == MealType.SNACK }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MealCard(iconRes: Int, titleRes: Int, meals: List<MealEntry>, modifier: Modifier) {
    val calories = meals.sumOf(MealEntry::calories)
    HealthElevatedCard(modifier = modifier.height(DashboardDimens.mealCardHeight), shape = Shape.large) {
        Column(modifier = Modifier.padding(Dimens.spaceSmall), verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
            Box(modifier = Modifier.size(Dimens.buttonHeightMedium).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                Text(text = stringResource(iconRes), style = MaterialTheme.typography.titleLarge)
            }
            Text(text = stringResource(titleRes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = stringResource(R.string.dashboard_meal_records, meals.size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = stringResource(R.string.dashboard_meal_kcal, calories), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun DailyTipCard() {
    DashboardCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(Dimens.spaceMedium), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(Dimens.buttonHeightLarge).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.dashboard_icon_tip), style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)) {
                Text(text = stringResource(R.string.dashboard_tip_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = stringResource(R.string.dashboard_tip_message), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = stringResource(R.string.dashboard_icon_arrow), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DashboardBottomBar(
    onNavigateToSettings: () -> Unit,
    onNavigateToMeals: () -> Unit,
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
            NavigationItem(R.string.dashboard_icon_activity_nav, R.string.dashboard_activity, false, {})
            NavigationItem(R.string.dashboard_icon_statistics, R.string.dashboard_statistics, false, onNavigateToStatistics)
            NavigationItem(R.string.dashboard_icon_settings, R.string.dashboard_settings, false, onNavigateToSettings)
        }
    }
}

@Composable
private fun RowScope.NavigationItem(iconRes: Int, labelRes: Int, selected: Boolean, onClick: () -> Unit) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier.weight(1f).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
    ) {
        Text(text = stringResource(iconRes), style = MaterialTheme.typography.headlineSmall, color = contentColor)
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
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
                userName = "Nguyễn An"
            ),
            onAction = {},
            onNavigateToSettings = {},
            onNavigateToStatistics = {}
        )
    }
}
