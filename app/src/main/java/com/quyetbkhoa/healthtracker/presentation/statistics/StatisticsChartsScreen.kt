package com.quyetbkhoa.healthtracker.presentation.statistics

import androidx.compose.runtime.Composable
import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange

@Composable
fun StatisticsChartsScreen(
    state: StatisticsUiState,
    onNavigateBack: () -> Unit,
    onRangeSelected: (StatisticsRange) -> Unit
) {
    StatisticsChartsContent(state, onNavigateBack, onRangeSelected)
}
