package com.quyetbkhoa.healthtracker.presentation.statistics

import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthIconText
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthOutlinedCard
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun StatisticsOverviewScreen(
    onNavigateToCharts: (StatisticsRange) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatisticsOverviewContent(
        state = state,
        onRangeSelected = { viewModel.onAction(StatisticsAction.SelectRange(it)) },
        onNavigateToCharts = onNavigateToCharts
    )
}

@Composable
private fun StatisticsOverviewContent(
    state: StatisticsUiState,
    onRangeSelected: (StatisticsRange) -> Unit,
    onNavigateToCharts: (StatisticsRange) -> Unit
) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
            return@Box
        }

        val pairedMetricRows = buildMetricRows(state)
        val isLargeFont = LocalDensity.current.fontScale > STATISTICS_LARGE_FONT_SCALE
        val metricRows = if (isLargeFont) {
            pairedMetricRows.flatten().map(::listOf)
        } else {
            pairedMetricRows
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = StatisticsDimens.screenPadding,
                vertical = StatisticsDimens.itemSpacing
            ),
            verticalArrangement = Arrangement.spacedBy(StatisticsDimens.itemSpacing)
        ) {
            item {
                StatisticsRangeSelector(
                    selectedRange = state.selectedRange,
                    onRangeSelected = onRangeSelected,
                    modifier = Modifier.statusBarsPadding()
                )
            }
            items(metricRows, key = { row -> row.joinToString { it.title } }) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(StatisticsDimens.itemSpacing)) {
                    row.forEach { metric -> MetricCard(metric, Modifier.weight(1f)) }
                }
            }
            item { ChartsButton { onNavigateToCharts(state.selectedRange) } }
            item { Spacer(Modifier.height(Dimens.spaceExtraSmall)) }
        }
    }
}

private data class MetricCardModel(
    val title: String,
    val value: String,
    val unit: String,
    val description: String,
    val history: String?,
    val containerColor: Color,
    val contentColor: Color
)

@Composable
private fun buildMetricRows(state: StatisticsUiState): List<List<MetricCardModel>> {
    val palette = statisticsPalette()
    val formatter = rememberStatisticsFormatter()
    val goalHistory = goalHistory(state.goal, formatter.date)
    val streakHistory = streakHistory(state.goal.longestStreak, formatter.date)

    @Composable
    fun metric(
        titleRes: Int,
        value: String,
        unitRes: Int,
        descriptionRes: Int,
        colors: Pair<Color, Color>,
        history: String? = null
    ) = MetricCardModel(
        title = stringResource(titleRes),
        value = value,
        unit = stringResource(unitRes),
        description = stringResource(descriptionRes),
        history = history,
        containerColor = colors.first,
        contentColor = colors.second
    )

    val burnedColors = palette.burnedContainer to palette.onBurnedContainer
    val consumedColors = palette.consumedContainer to palette.onConsumedContainer
    val goalColors = palette.goalContainer to palette.onGoalContainer
    val balanceColors = palette.balanceContainer to palette.onBalanceContainer

    return listOf(
        listOf(
            metric(R.string.statistics_total_burned, formatter.number(state.burned.total), R.string.unit_kcal, R.string.statistics_info_total_burned, burnedColors),
            metric(R.string.statistics_average_burned, formatter.number(state.burned.dailyAverage), R.string.statistics_unit_kcal_day, R.string.statistics_info_average_burned, burnedColors)
        ),
        listOf(
            metric(R.string.statistics_total_consumed, formatter.number(state.consumed.total), R.string.unit_kcal, R.string.statistics_info_total_consumed, consumedColors),
            metric(R.string.statistics_average_consumed, formatter.number(state.consumed.dailyAverage), R.string.statistics_unit_kcal_day, R.string.statistics_info_average_consumed, consumedColors)
        ),
        listOf(
            metric(R.string.statistics_goal_days, stringResource(R.string.statistics_fraction, formatter.number(state.goal.achievedDays), formatter.number(state.goal.totalDays)), R.string.statistics_unit_days, R.string.statistics_info_goal_days, goalColors, goalHistory),
            metric(R.string.statistics_target_difference, formatter.signed(state.goal.targetDifference), R.string.unit_kcal, R.string.statistics_info_target_difference, balanceColors)
        ),
        listOf(
            metric(R.string.statistics_goal_streak, formatter.number(state.goal.longestStreak.length), R.string.statistics_unit_days, R.string.statistics_info_goal_streak, goalColors, streakHistory),
            metric(R.string.statistics_highest_consumed, formatter.number(state.consumed.highest.value), R.string.unit_kcal, R.string.statistics_info_highest_consumed, consumedColors, peakHistory(state.consumed.highest, formatter.date, R.string.statistics_no_consumed_history))
        ),
        listOf(
            metric(R.string.statistics_highest_burned, formatter.number(state.burned.highest.value), R.string.unit_kcal, R.string.statistics_info_highest_burned, burnedColors, peakHistory(state.burned.highest, formatter.date, R.string.statistics_no_burned_history)),
            metric(R.string.statistics_goal_rate, stringResource(R.string.statistics_percent, state.goal.achievementRate), R.string.statistics_goal_rate_detail, R.string.statistics_info_goal_rate, goalColors, goalHistory)
        )
    )
}

@Composable
private fun MetricCard(model: MetricCardModel, modifier: Modifier = Modifier) {
    var isDialogVisible by remember { mutableStateOf(false) }
    HealthElevatedCard(
        modifier = modifier.height(StatisticsDimens.metricCardHeight)
            .clickable(role = Role.Button) { isDialogVisible = true },
        shape = Shape.extraLarge,
        colors = CardDefaults.cardColors(containerColor = model.containerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(StatisticsDimens.cardPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = model.title,
                modifier = Modifier.fillMaxWidth(),
                color = model.contentColor,
                fontSize = StatisticsDimens.metricTitleSize,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = model.value,
                    color = model.contentColor,
                    fontSize = StatisticsDimens.metricValueSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(model.unit, color = model.contentColor.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
    }
    if (isDialogVisible) MetricDetailsDialog(model) { isDialogVisible = false }
}

@Composable
private fun MetricDetailsDialog(model: MetricCardModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(model.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(StatisticsDimens.itemSpacing)) {
                Text(stringResource(R.string.statistics_dialog_value, model.value, model.unit), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                model.history?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                Text(model.description)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.statistics_close)) } }
    )
}

@Composable
private fun ChartsButton(onClick: () -> Unit) {
    HealthOutlinedCard(
        modifier = Modifier.fillMaxWidth().height(StatisticsDimens.chartsButtonHeight)
            .clickable(role = Role.Button, onClick = onClick),
        shape = Shape.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = Dimens.spaceMedium), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.statistics_charts), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.statistics_charts_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            HealthIconText(
                text = stringResource(R.string.dashboard_icon_arrow),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = StatisticsDimens.chartsButtonArrowSize)
            )
        }
    }
}

private data class StatisticsFormatter(
    val number: (Int) -> String,
    val signed: (Int) -> String,
    val date: DateTimeFormatter
)

@Composable
private fun rememberStatisticsFormatter(): StatisticsFormatter {
    val locale = LocalConfiguration.current.locales[0]
    val numberFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val datePattern = stringResource(R.string.statistics_date_pattern)
    val dateFormat = remember(locale, datePattern) { DateTimeFormatter.ofPattern(datePattern, locale) }
    val positivePattern = stringResource(R.string.statistics_positive_number)
    return StatisticsFormatter(
        number = numberFormat::format,
        signed = { value -> if (value > 0) positivePattern.format(numberFormat.format(value)) else numberFormat.format(value) },
        date = dateFormat
    )
}

private const val STATISTICS_LARGE_FONT_SCALE = 1.15f

@Composable
private fun streakHistory(streak: GoalStreak, formatter: DateTimeFormatter): String = when {
    streak.startDate == null || streak.endDate == null -> stringResource(R.string.statistics_no_streak_history)
    streak.startDate == streak.endDate -> stringResource(R.string.statistics_history_single_date, streak.startDate.format(formatter))
    else -> stringResource(R.string.statistics_history_date_range, streak.startDate.format(formatter), streak.endDate.format(formatter))
}

@Composable
private fun goalHistory(goal: GoalStatistics, formatter: DateTimeFormatter): String = when {
    goal.firstAchievedDate == null || goal.lastAchievedDate == null -> stringResource(R.string.statistics_no_goal_history)
    goal.firstAchievedDate == goal.lastAchievedDate -> stringResource(R.string.statistics_goal_history_single, goal.firstAchievedDate.format(formatter))
    else -> stringResource(R.string.statistics_goal_history_range, goal.firstAchievedDate.format(formatter), goal.lastAchievedDate.format(formatter))
}

@Composable
private fun peakHistory(peak: DatedStatistic, formatter: DateTimeFormatter, emptyMessageRes: Int): String =
    peak.date?.let { stringResource(R.string.statistics_highest_date, it.format(formatter)) }
        ?: stringResource(emptyMessageRes)

@Preview
@Composable
private fun PreviewStatisticsOverviewScreen() {
    val today = LocalDate.of(2026, 7, 24)
    HealthTrackerTheme {
        StatisticsOverviewContent(
            state = StatisticsUiState(
                isLoading = false,
                selectedRange = StatisticsRange.LAST_7_DAYS,
                dailyTarget = 2_250,
                consumed = CalorieStatistics(
                    total = 14_680,
                    dailyAverage = 2_097,
                    highest = DatedStatistic(2_420, today.minusDays(2))
                ),
                burned = CalorieStatistics(
                    total = 2_730,
                    dailyAverage = 390,
                    highest = DatedStatistic(610, today.minusDays(1))
                ),
                goal = GoalStatistics(
                    achievedDays = 5,
                    totalDays = 7,
                    targetDifference = -153,
                    achievementRate = 71,
                    longestStreak = GoalStreak(
                        length = 3,
                        startDate = today.minusDays(5),
                        endDate = today.minusDays(3)
                    ),
                    firstAchievedDate = today.minusDays(6),
                    lastAchievedDate = today.minusDays(1)
                )
            ),
            onRangeSelected = {},
            onNavigateToCharts = {}
        )
    }
}
