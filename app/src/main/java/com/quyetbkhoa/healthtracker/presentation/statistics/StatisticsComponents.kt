package com.quyetbkhoa.healthtracker.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens

internal data class StatisticsPalette(
    val consumed: Color,
    val consumedContainer: Color,
    val onConsumedContainer: Color,
    val burned: Color,
    val burnedContainer: Color,
    val onBurnedContainer: Color,
    val balance: Color,
    val balanceContainer: Color,
    val onBalanceContainer: Color,
    val goalContainer: Color,
    val onGoalContainer: Color
)

@Composable
internal fun statisticsPalette(): StatisticsPalette = MaterialTheme.colorScheme.run {
    StatisticsPalette(
        consumed = primary,
        consumedContainer = primaryContainer,
        onConsumedContainer = onPrimaryContainer,
        burned = error,
        burnedContainer = errorContainer,
        onBurnedContainer = onErrorContainer,
        balance = tertiary,
        balanceContainer = tertiaryContainer,
        onBalanceContainer = onTertiaryContainer,
        goalContainer = secondaryContainer,
        onGoalContainer = onSecondaryContainer
    )
}

@Composable
internal fun StatisticsRangeSelector(
    selectedRange: StatisticsRange,
    onRangeSelected: (StatisticsRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(StatisticsDimens.rangeHeight),
        shape = RoundedCornerShape(StatisticsDimens.rangeOuterRadius),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            Dimens.borderWidthThin,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(Modifier.fillMaxSize().padding(Dimens.spaceExtraSmall)) {
            RANGE_OPTIONS.forEach { option ->
                RangeOption(
                    option = option,
                    isSelected = option.range == selectedRange,
                    onClick = { onRangeSelected(option.range) },
                    modifier = Modifier.weight(option.weight)
                )
            }
        }
    }
}

@Composable
private fun RangeOption(
    option: RangeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier.fillMaxSize()
            .background(containerColor, RoundedCornerShape(StatisticsDimens.rangeInnerRadius))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(option.labelRes),
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

private data class RangeOption(val range: StatisticsRange, val labelRes: Int, val weight: Float = 1f)

private val RANGE_OPTIONS = listOf(
    RangeOption(StatisticsRange.TODAY, R.string.statistics_today),
    RangeOption(StatisticsRange.LAST_7_DAYS, R.string.statistics_last_7_days),
    RangeOption(StatisticsRange.LAST_30_DAYS, R.string.statistics_last_30_days, 1.35f),
    RangeOption(StatisticsRange.ALL, R.string.statistics_all)
)
