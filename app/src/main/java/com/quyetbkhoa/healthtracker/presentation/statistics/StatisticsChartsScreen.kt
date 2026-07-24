package com.quyetbkhoa.healthtracker.presentation.statistics

import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthMarqueeText as Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthElevatedCard
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun StatisticsChartsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.selectedRange) {
        if (state.selectedRange == StatisticsRange.ALL) {
            viewModel.onAction(StatisticsAction.SelectRange(StatisticsRange.LAST_30_DAYS))
        }
    }
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
        if (state.isLoading || state.selectedRange == StatisticsRange.ALL) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
            return@Box
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(StatisticsDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(StatisticsDimens.screenPadding)
        ) {
            item { ChartsHeader(onNavigateBack) }
            item {
                StatisticsRangeSelector(
                    selectedRange = state.selectedRange,
                    onRangeSelected = onRangeSelected,
                    showAllOption = false
                )
            }
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
private data class ChartAxisLabel(val pointIndex: Int, val text: String)

@Composable
private fun rememberChartPoints(state: StatisticsUiState): List<ChartPoint> {
    val locale = LocalConfiguration.current.locales[0]
    val datePattern = stringResource(R.string.statistics_chart_date_pattern)
    val formatter = remember(locale, datePattern) { DateTimeFormatter.ofPattern(datePattern, locale) }

    return remember(state.dailyStatistics, formatter) {
        state.dailyStatistics.map { day ->
            ChartPoint(
                label = day.date.format(formatter),
                consumed = day.consumedCalories,
                burned = day.burnedCalories
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
            ChartLabels(
                pointCount = points.size,
                labels = points.axisLabels(state.selectedRange)
            )
            ChartLegend(palette)
        }
    }
}

@Composable
private fun BarChart(points: List<ChartPoint>, palette: StatisticsPalette) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axisColor = MaterialTheme.colorScheme.outline
    val maximum = niceAxisMaximum(
        value = points.maxOf { maxOf(it.consumed, it.burned) },
        segments = CHART_GRID_LINES - 1
    )
    val axisValues = List(CHART_GRID_LINES) { index ->
        maximum - maximum * index / (CHART_GRID_LINES - 1)
    }

    Row(Modifier.fillMaxWidth()) {
        YAxisLabels(axisValues)
        Canvas(
            Modifier
                .weight(1f)
                .height(StatisticsDimens.chartHeight)
        ) {
            val chartInset = StatisticsDimens.chartVerticalInset.toPx()
            val baseline = size.height - chartInset
            val chartHeight = baseline - chartInset
            val slotWidth = size.width / points.size
            val barWidth = (slotWidth * BAR_WIDTH_RATIO)
                .coerceAtMost(StatisticsDimens.chartBarMaxWidth.toPx())
            val barGap = (slotWidth * BAR_GAP_RATIO)
                .coerceAtMost(Dimens.spaceExtraSmall.toPx())

            repeat(CHART_GRID_LINES) { index ->
                val y = chartInset + chartHeight * index / (CHART_GRID_LINES - 1)
                drawLine(
                    gridColor,
                    Offset(0f, y),
                    Offset(size.width, y),
                    StatisticsDimens.chartGridWidth.toPx()
                )
            }
            drawLine(
                axisColor,
                Offset(0f, chartInset),
                Offset(0f, baseline),
                StatisticsDimens.chartAxisWidthStroke.toPx()
            )
            points.forEachIndexed { index, point ->
                val center = slotWidth * (index + HALF_SLOT)
                drawCalorieBar(
                    x = center - barWidth - barGap / 2,
                    value = point.consumed,
                    maximum = maximum,
                    baseline = baseline,
                    chartHeight = chartHeight,
                    width = barWidth,
                    color = palette.consumed
                )
                drawCalorieBar(
                    x = center + barGap / 2,
                    value = point.burned,
                    maximum = maximum,
                    baseline = baseline,
                    chartHeight = chartHeight,
                    width = barWidth,
                    color = palette.burned
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCalorieBar(
    x: Float,
    value: Int,
    maximum: Int,
    baseline: Float,
    chartHeight: Float,
    width: Float,
    color: Color
) {
    val height = chartHeight * value / maximum
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
            ChartLabels(
                pointCount = points.size,
                labels = points.axisLabels(state.selectedRange)
            )
        }
    }
}

@Composable
private fun LineChart(values: List<Int>, lineColor: Color) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axisColor = MaterialTheme.colorScheme.outline
    val pointCenterColor = MaterialTheme.colorScheme.background
    val maximum = niceAxisMaximum(
        value = values.maxOf { it.absoluteValue },
        segments = CHART_LINE_GRID_LINES / 2
    )
    val axisValues = List(CHART_LINE_GRID_LINES) { index ->
        maximum - maximum * index / (CHART_LINE_GRID_LINES / 2)
    }

    Row(Modifier.fillMaxWidth()) {
        YAxisLabels(axisValues)
        Canvas(
            Modifier
                .weight(1f)
                .height(StatisticsDimens.chartHeight)
        ) {
            val centerY = size.height / 2
            val chartAmplitude = centerY - StatisticsDimens.chartVerticalInset.toPx()
            repeat(CHART_LINE_GRID_LINES) { index ->
                val y = StatisticsDimens.chartVerticalInset.toPx() +
                    chartAmplitude * 2 * index / (CHART_LINE_GRID_LINES - 1)
                drawLine(
                    gridColor,
                    Offset(0f, y),
                    Offset(size.width, y),
                    StatisticsDimens.chartGridWidth.toPx()
                )
            }
            drawLine(
                axisColor,
                Offset(0f, StatisticsDimens.chartVerticalInset.toPx()),
                Offset(0f, size.height - StatisticsDimens.chartVerticalInset.toPx()),
                StatisticsDimens.chartAxisWidthStroke.toPx()
            )
            val slotWidth = size.width / values.size
            val pointRadius = minOf(
                StatisticsDimens.chartPointRadius.toPx(),
                slotWidth * CHART_POINT_RADIUS_SLOT_RATIO
            )
            val path = Path()
            values.forEachIndexed { index, value ->
                val x = slotWidth * (index + HALF_SLOT)
                val y = centerY - value.toFloat() / maximum * chartAmplitude
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path,
                lineColor,
                style = Stroke(StatisticsDimens.chartLineWidth.toPx(), cap = StrokeCap.Round)
            )
            values.forEachIndexed { index, value ->
                val x = slotWidth * (index + HALF_SLOT)
                val y = centerY - value.toFloat() / maximum * chartAmplitude
                drawCircle(lineColor, pointRadius, Offset(x, y))
                drawCircle(
                    pointCenterColor,
                    pointRadius * CHART_POINT_INNER_RATIO,
                    Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun YAxisLabels(values: List<Int>) {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    Column(
        modifier = Modifier
            .width(StatisticsDimens.chartAxisWidth)
            .height(StatisticsDimens.chartHeight)
            .padding(
                end = StatisticsDimens.compactSpacing,
                top = StatisticsDimens.chartVerticalInset,
                bottom = StatisticsDimens.chartVerticalInset
            ),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        values.forEach { value ->
            Text(
                text = formatter.format(value),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = StatisticsDimens.chartLabelSize,
                maxLines = 1
            )
        }
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
private fun ChartLabels(pointCount: Int, labels: List<ChartAxisLabel>) {
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .padding(start = StatisticsDimens.chartAxisWidth)
            .height(StatisticsDimens.chartLabelHeight)
    ) {
        if (pointCount == 0) return@BoxWithConstraints
        val labelWidth = StatisticsDimens.chartDateLabelWidth.coerceAtMost(maxWidth)
        val maximumOffset = (maxWidth - labelWidth).coerceAtLeast(0.dp)
        labels.forEach { label ->
            val center = maxWidth * ((label.pointIndex + HALF_SLOT) / pointCount)
            val x = (center - labelWidth / 2).coerceIn(0.dp, maximumOffset)
            Text(
                text = label.text,
                modifier = Modifier
                    .offset(x = x)
                    .width(labelWidth),
                textAlign = TextAlign.Center,
                fontSize = StatisticsDimens.chartLabelSize,
                maxLines = 1
            )
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

private fun List<ChartPoint>.axisLabels(range: StatisticsRange): List<ChartAxisLabel> =
    mapIndexedNotNull { index, point ->
        val shouldShow = when (range) {
            StatisticsRange.TODAY, StatisticsRange.LAST_7_DAYS -> true
            StatisticsRange.LAST_30_DAYS -> index % DAYS_PER_AXIS_LABEL == 0
            StatisticsRange.ALL -> false
        }
        point.takeIf { shouldShow }?.let { ChartAxisLabel(index, it.label) }
    }

private fun niceAxisMaximum(value: Int, segments: Int): Int {
    if (value <= 0 || segments <= 0) return segments.coerceAtLeast(1)
    val roughStep = value.toDouble() / segments
    val magnitude = 10.0.pow(floor(log10(roughStep)))
    val normalizedStep = roughStep / magnitude
    val niceStep = when {
        normalizedStep <= 1.0 -> 1.0
        normalizedStep <= 2.0 -> 2.0
        normalizedStep <= 2.5 -> 2.5
        normalizedStep <= 5.0 -> 5.0
        else -> 10.0
    } * magnitude
    return (niceStep.toInt().coerceAtLeast(1) * segments)
}

private const val DAYS_PER_AXIS_LABEL = 7
private const val CHART_GRID_LINES = 4
private const val CHART_LINE_GRID_LINES = 5
private const val BAR_WIDTH_RATIO = 0.22f
private const val BAR_GAP_RATIO = 0.08f
private const val HALF_SLOT = 0.5f
private const val CHART_POINT_RADIUS_SLOT_RATIO = 0.28f
private const val CHART_POINT_INNER_RATIO = 0.5f

@Preview
@Composable
private fun PreviewStatisticsChartsScreen() {
    val today = LocalDate.of(2026, 7, 24)
    HealthTrackerTheme {
        StatisticsChartsContent(
            state = StatisticsUiState(
                isLoading = false,
                selectedRange = StatisticsRange.LAST_7_DAYS,
                dailyTarget = 2_250,
                consumed = CalorieStatistics(total = 14_680, dailyAverage = 2_097),
                burned = CalorieStatistics(total = 2_730, dailyAverage = 390),
                goal = GoalStatistics(
                    achievedDays = 5,
                    totalDays = 7,
                    targetDifference = -153,
                    achievementRate = 71
                ),
                dailyStatistics = List(7) { index ->
                    DailyStatistic(
                        date = today.minusDays((6 - index).toLong()),
                        consumedCalories = 1_880 + index * 95,
                        burnedCalories = 260 + index * 40
                    )
                }
            ),
            onNavigateBack = {},
            onRangeSelected = {}
        )
    }
}
