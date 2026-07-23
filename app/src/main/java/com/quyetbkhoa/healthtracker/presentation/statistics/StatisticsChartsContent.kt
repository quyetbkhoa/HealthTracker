package com.quyetbkhoa.healthtracker.presentation.statistics

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange
import com.quyetbkhoa.healthtracker.presentation.designsystem.Dimens
import com.quyetbkhoa.healthtracker.presentation.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.presentation.designsystem.Shape
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthMarqueeText as Text
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthElevatedCard
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

@Composable
internal fun StatisticsChartsContent(
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
    Row(
        Modifier.statusBarsPadding().fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.navigate_back))
        }
        Text(
            stringResource(R.string.statistics_charts),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class ChartPoint(val label: String, val consumed: Int, val burned: Int)

@Composable
private fun rememberChartPoints(state: StatisticsUiState): List<ChartPoint> {
    val locale = LocalConfiguration.current.locales[0]
    val pattern = stringResource(R.string.statistics_chart_date_pattern)
    val formatter = remember(locale, pattern) { DateTimeFormatter.ofPattern(pattern, locale) }
    return remember(state.dailyStatistics, formatter) {
        state.dailyStatistics.map {
            ChartPoint(
                label = it.date.format(formatter),
                consumed = it.consumedCalories,
                burned = it.burnedCalories
            )
        }
    }
}

@Composable
private fun CaloriesBarChart(state: StatisticsUiState) {
    val points = rememberChartPoints(state)
    val palette = statisticsPalette()
    ChartCard(
        title = stringResource(R.string.statistics_chart_calories),
        unit = stringResource(R.string.statistics_chart_kcal)
    ) {
        if (points.hasNoCalories()) {
            EmptyChart()
        } else {
            VicoBarChart(points, state.selectedRange, palette)
            ChartLegend(palette)
        }
    }
}

@Composable
private fun VicoBarChart(
    points: List<ChartPoint>,
    range: StatisticsRange,
    palette: StatisticsPalette
) {
    val model = remember(points) {
        CartesianChartModel(
            ColumnCartesianLayerModel.build {
                series(y = points.map(ChartPoint::consumed))
                series(y = points.map(ChartPoint::burned))
            }
        )
    }
    VicoChart(points.labels(range), palette) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            fill = Fill(palette.consumed),
                            thickness = StatisticsDimens.chartBarMaxWidth,
                            shape = CircleShape
                        ),
                        rememberLineComponent(
                            fill = Fill(palette.burned),
                            thickness = StatisticsDimens.chartBarMaxWidth,
                            shape = CircleShape
                        )
                    ),
                    columnCollectionSpacing = StatisticsDimens.compactSpacing
                ),
                startAxis = chartStartAxis(),
                bottomAxis = chartBottomAxis(it),
                getXStep = { _, _, _ -> 1.0 }
            ),
            model = model,
            modifier = Modifier.fillMaxWidth().height(StatisticsDimens.chartHeight),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            zoomState = rememberVicoZoomState(
                zoomEnabled = false,
                initialZoom = Zoom.Content
            )
        )
    }
}

@Composable
private fun TargetDifferenceChart(state: StatisticsUiState) {
    val points = rememberChartPoints(state)
    val palette = statisticsPalette()
    ChartCard(
        title = stringResource(R.string.statistics_chart_balance),
        unit = stringResource(R.string.statistics_chart_kcal_day)
    ) {
        if (points.hasNoCalories()) {
            EmptyChart()
        } else {
            VicoLineChart(
                values = points.map { it.consumed - state.dailyTarget },
                labels = points.labels(state.selectedRange),
                palette = palette
            )
        }
    }
}

@Composable
private fun VicoLineChart(
    values: List<Int>,
    labels: Map<Int, String>,
    palette: StatisticsPalette
) {
    val maximum = values.maxOf { it.absoluteValue }.coerceAtLeast(1).toDouble()
    val model = remember(values) {
        CartesianChartModel(LineCartesianLayerModel.build { series(y = values) })
    }
    VicoChart(labels, palette) { bottomAxis ->
        val point = LineCartesianLayer.Point(
            component = rememberShapeComponent(
                fill = Fill(MaterialTheme.colorScheme.surface),
                shape = CircleShape,
                strokeFill = Fill(palette.balance),
                strokeThickness = 2.dp
            ),
            size = 9.dp
        )
        val line = LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(Fill(palette.balance)),
            stroke = LineCartesianLayer.LineStroke.Continuous(
                thickness = StatisticsDimens.chartLineWidth,
                cap = StrokeCap.Round
            ),
            areaFill = LineCartesianLayer.AreaFill.single(
                Fill(palette.balance.copy(alpha = 0.12f))
            ),
            pointProvider = LineCartesianLayer.PointProvider.single(point)
        )
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(line),
                    rangeProvider = CartesianLayerRangeProvider.fixed(
                        minY = -maximum,
                        maxY = maximum
                    )
                ),
                startAxis = chartStartAxis(),
                bottomAxis = chartBottomAxis(bottomAxis),
                getXStep = { _, _, _ -> 1.0 }
            ),
            model = model,
            modifier = Modifier.fillMaxWidth().height(StatisticsDimens.chartHeight),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            zoomState = rememberVicoZoomState(
                zoomEnabled = false,
                initialZoom = Zoom.Content
            )
        )
    }
}

@Composable
private fun VicoChart(
    labels: Map<Int, String>,
    palette: StatisticsPalette,
    content: @Composable (CartesianValueFormatter) -> Unit
) {
    val valueFormatter = remember(labels) {
        CartesianValueFormatter { _, value, _ -> labels[value.toInt()].orEmpty() }
    }
    ProvideVicoTheme(
        theme = rememberM3VicoTheme(
            columnCartesianLayerColors = listOf(palette.consumed, palette.burned),
            lineCartesianLayerColors = listOf(palette.balance),
            lineColor = MaterialTheme.colorScheme.outlineVariant,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        content(valueFormatter)
    }
}

@Composable
private fun chartStartAxis(): VerticalAxis<Axis.Position.Vertical.Start> {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val valueFormatter = remember(formatter) {
        CartesianValueFormatter { _, value, _ -> formatter.format(value) }
    }
    return VerticalAxis.rememberStart(
        line = null,
        tick = null,
        label = rememberAxisLabelComponent(
            MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = StatisticsDimens.chartLabelSize
            )
        ),
        valueFormatter = valueFormatter
    )
}

@Composable
private fun chartBottomAxis(
    valueFormatter: CartesianValueFormatter
): HorizontalAxis<Axis.Position.Horizontal.Bottom> =
    HorizontalAxis.rememberBottom(
        line = null,
        tick = null,
        guideline = null,
        label = rememberAxisLabelComponent(
            MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = StatisticsDimens.chartLabelSize
            )
        ),
        valueFormatter = valueFormatter
    )

@Composable
private fun ChartCard(title: String, unit: String, content: @Composable () -> Unit) {
    HealthElevatedCard(Modifier.fillMaxWidth(), shape = Shape.extraLarge) {
        Column(
            Modifier.padding(Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(StatisticsDimens.itemSpacing)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(unit, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
private fun EmptyChart() {
    Box(
        Modifier.fillMaxWidth().height(StatisticsDimens.emptyChartHeight),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.statistics_no_data),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        Column(
            Modifier.padding(Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(StatisticsDimens.itemSpacing)
        ) {
            Text(
                stringResource(R.string.statistics_period_summary),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            SummaryRow(
                stringResource(R.string.statistics_average_consumed),
                state.consumed.dailyAverage,
                palette.consumed
            )
            SummaryRow(
                stringResource(R.string.statistics_average_burned),
                state.burned.dailyAverage,
                palette.burned
            )
            SummaryRow(
                stringResource(R.string.statistics_target_difference),
                state.goal.targetDifference,
                palette.balance,
                isSigned = true
            )
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
            stringResource(
                if (isSigned && value > 0) {
                    R.string.statistics_positive_kcal_value
                } else {
                    R.string.dashboard_kcal_value
                },
                formatted
            ),
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun List<ChartPoint>.hasNoCalories(): Boolean =
    isEmpty() || all { it.consumed == 0 && it.burned == 0 }

private fun List<ChartPoint>.labels(range: StatisticsRange): Map<Int, String> =
    mapIndexedNotNull { index, point ->
        val visible = when (range) {
            StatisticsRange.TODAY, StatisticsRange.LAST_7_DAYS -> true
            StatisticsRange.LAST_30_DAYS -> index % DAYS_PER_AXIS_LABEL == 0
            StatisticsRange.ALL -> false
        }
        if (visible) index to point.label else null
    }.toMap()

private const val DAYS_PER_AXIS_LABEL = 7

@Preview
@Composable
private fun PreviewStatisticsChartsScreen() {
    val today = LocalDate.of(2026, 7, 24)
    HealthTrackerTheme {
        StatisticsChartsScreen(
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
