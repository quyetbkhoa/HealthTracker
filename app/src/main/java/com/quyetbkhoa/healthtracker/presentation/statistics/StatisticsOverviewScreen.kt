package com.quyetbkhoa.healthtracker.presentation.statistics

import androidx.compose.runtime.Composable
import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange

@Composable
fun StatisticsOverviewScreen(
    state: StatisticsUiState,
    onRangeSelected: (StatisticsRange) -> Unit,
    onNavigateToCharts: (StatisticsRange) -> Unit
) {
    StatisticsOverviewContent(state, onRangeSelected, onNavigateToCharts)
}
