package com.quyetbkhoa.healthtracker.presentation.statistics

import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthElevatedCard
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

@Composable
fun StatisticsChartsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatisticsChartsContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onRangeSelected = { viewModel.onAction(StatisticsAction.SelectRange(it)) }
    )
}

@Composable
private fun StatisticsChartsContent(
    state: StatisticsUiState,
    onNavigateBack: () -> Unit,
    onRangeSelected: (StatisticsRange) -> Unit
) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
            return@Box
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(StatisticsDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(StatisticsDimens.screenPadding)
        ) {
            item { ChartsHeader(onNavigateBack) }
            item { StatisticsRangeSelector(state.selectedRange, onRangeSelected) }
            item { CaloriesBarChart(state) }
            item { TargetDifferenceChart(state) }
            item { PeriodSummary(state) }
        }
    }
}

@Composable
private fun ChartsHeader(onNavigateBack: () -> Unit) {
    Row(Modifier.statusBarsPadding().fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.navigate_back))
        }
        Text(stringResource(R.string.statistics_charts), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

private data class ChartPoint(val label: String, val consumed: Int, val burned: Int)

@Composable
private fun rememberChartPoints(state: StatisticsUiState): List<ChartPoint> {
    val locale = LocalConfiguration.current.locales[0]
    val datePattern = stringResource(R.string.statistics_chart_date_pattern)
    val rangePattern = stringResource(R.string.statistics_chart_range)
    val formatter = remember(locale, datePattern) { DateTimeFormatter.ofPattern(datePattern, locale) }
    val chunkSize = state.selectedRange.chartChunkSize(state.dailyStatistics.size)

    return remember(state.dailyStatistics, chunkSize, formatter, rangePattern) {
        state.dailyStatistics.chunked(chunkSize).map { days ->
            val start = days.first().date.format(formatter)
            val end = days.last().date.format(formatter)
            ChartPoint(
                label = if (days.size == 1) start else rangePattern.format(start, end),
                consumed = days.sumOf(DailyStatistic::consumedCalories) / days.size,
                burned = days.sumOf(DailyStatistic::burnedCalories) / days.size
            )
        }
    }
}

@Composable
private fun CaloriesBarChart(state: StatisticsUiState) {
    val points = rememberChartPoints(state)
    val palette = statisticsPalette()
    ChartCard(stringResource(R.string.statistics_chart_calories), stringResource(R.string.statistics_chart_kcal)) {
        if (points.hasNoCalories()) {
            EmptyChart()
        } else {
            BarChart(points, palette)
            ChartLabels(points.map(ChartPoint::label))
            ChartLegend(palette)
        }
    }
}

@Composable
private fun BarChart(points: List<ChartPoint>, palette: StatisticsPalette) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val maximum = points.maxOf { maxOf(it.consumed, it.burned) }.coerceAtLeast(1)
    Canvas(Modifier.fillMaxWidth().height(StatisticsDimens.chartHeight)) {
        val baseline = size.height - StatisticsDimens.chartBaselineInset.toPx()
        val slotWidth = size.width / points.size
        val barWidth = (slotWidth * BAR_WIDTH_RATIO).coerceAtMost(StatisticsDimens.chartBarMaxWidth.toPx())

        repeat(CHART_GRID_LINES) { index ->
            val y = baseline * index / (CHART_GRID_LINES - 1)
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), StatisticsDimens.chartGridWidth.toPx())
        }
        points.forEachIndexed { index, point ->
            val center = slotWidth * (index + HALF_SLOT)
            drawCalorieBar(center - barWidth - Dimens.spaceExtraSmall.toPx(), point.consumed, maximum, baseline, barWidth, palette.consumed)
            drawCalorieBar(center + Dimens.spaceExtraSmall.toPx(), point.burned, maximum, baseline, barWidth, palette.burned)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCalorieBar(
    x: Float,
    value: Int,
    maximum: Int,
    baseline: Float,
    width: Float,
    color: Color
) {
    val height = baseline * value / maximum
    drawRoundRect(color, Offset(x, baseline - height), Size(width, height), CornerRadius(width / 2))
}

@Composable
private fun TargetDifferenceChart(state: StatisticsUiState) {
    val points = rememberChartPoints(state)
    val palette = statisticsPalette()
    ChartCard(stringResource(R.string.statistics_chart_balance), stringResource(R.string.statistics_chart_kcal_day)) {
        if (points.hasNoCalories()) {
            EmptyChart()
        } else {
            LineChart(points.map { it.consumed - state.dailyTarget }, palette.balance)
            ChartLabels(points.map(ChartPoint::label))
        }
    }
}

@Composable
private fun LineChart(values: List<Int>, lineColor: Color) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val pointCenterColor = MaterialTheme.colorScheme.background
    val maximum = values.maxOf { it.absoluteValue }.coerceAtLeast(1)
    Canvas(Modifier.fillMaxWidth().height(StatisticsDimens.chartHeight)) {
        val centerY = size.height / 2
        drawLine(gridColor, Offset(0f, centerY), Offset(size.width, centerY), StatisticsDimens.chartGridWidth.toPx())
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = if (values.size == 1) size.width / 2 else size.width * index / (values.size - 1)
            val y = centerY - value.toFloat() / maximum * (centerY - StatisticsDimens.chartLineInset.toPx())
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(lineColor, StatisticsDimens.chartPointRadius.toPx(), Offset(x, y))
            drawCircle(pointCenterColor, StatisticsDimens.chartPointInnerRadius.toPx(), Offset(x, y))
        }
        drawPath(path, lineColor, style = Stroke(StatisticsDimens.chartLineWidth.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun ChartCard(title: String, unit: String, content: @Composable () -> Unit) {
    HealthElevatedCard(Modifier.fillMaxWidth(), shape = Shape.extraLarge) {
        Column(Modifier.padding(Dimens.spaceMedium), verticalArrangement = Arrangement.spacedBy(StatisticsDimens.itemSpacing)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(unit, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
private fun EmptyChart() {
    Box(Modifier.fillMaxWidth().height(StatisticsDimens.emptyChartHeight), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.statistics_no_data), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ChartLabels(labels: List<String>) {
    Row(Modifier.fillMaxWidth()) {
        labels.forEach { label ->
            Text(label, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = StatisticsDimens.chartLabelSize, maxLines = 2)
        }
    }
}

@Composable
private fun ChartLegend(palette: StatisticsPalette) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        LegendItem(palette.consumed, stringResource(R.string.statistics_consumed))
        Spacer(Modifier.width(Dimens.spaceLarge))
        LegendItem(palette.burned, stringResource(R.string.statistics_burned))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(StatisticsDimens.legendDotSize).clip(CircleShape).background(color))
        Spacer(Modifier.width(StatisticsDimens.compactSpacing))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun PeriodSummary(state: StatisticsUiState) {
    val palette = statisticsPalette()
    HealthElevatedCard(Modifier.fillMaxWidth(), shape = Shape.extraLarge) {
        Column(Modifier.padding(Dimens.spaceMedium), verticalArrangement = Arrangement.spacedBy(StatisticsDimens.itemSpacing)) {
            Text(stringResource(R.string.statistics_period_summary), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            SummaryRow(stringResource(R.string.statistics_average_consumed), state.consumed.dailyAverage, palette.consumed)
            SummaryRow(stringResource(R.string.statistics_average_burned), state.burned.dailyAverage, palette.burned)
            SummaryRow(stringResource(R.string.statistics_target_difference), state.goal.targetDifference, palette.balance, isSigned = true)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Int, color: Color, isSigned: Boolean = false) {
    val locale = LocalConfiguration.current.locales[0]
    val formatted = NumberFormat.getIntegerInstance(locale).format(value)
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            stringResource(if (isSigned && value > 0) R.string.statistics_positive_kcal_value else R.string.dashboard_kcal_value, formatted),
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun List<ChartPoint>.hasNoCalories(): Boolean = isEmpty() || all { it.consumed == 0 && it.burned == 0 }

private fun StatisticsRange.chartChunkSize(dayCount: Int): Int = when (this) {
    StatisticsRange.TODAY, StatisticsRange.LAST_7_DAYS -> 1
    StatisticsRange.LAST_30_DAYS -> DAYS_PER_CHART_GROUP
    StatisticsRange.ALL -> maxOf(1, (dayCount + MAX_CHART_POINTS - 1) / MAX_CHART_POINTS)
}

private const val DAYS_PER_CHART_GROUP = 5
private const val MAX_CHART_POINTS = 8
private const val CHART_GRID_LINES = 4
private const val BAR_WIDTH_RATIO = 0.22f
private const val HALF_SLOT = 0.5f
