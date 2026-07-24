package com.quyetbkhoa.healthtracker.presentation.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange

@Composable
fun StatisticsOverviewRoute(
    onNavigateToCharts: (StatisticsRange) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatisticsOverviewScreen(
        state = state,
        onRangeSelected = { viewModel.onAction(StatisticsAction.SelectRange(it)) },
        onNavigateToCharts = onNavigateToCharts
    )
}

@Composable
fun StatisticsChartsRoute(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.selectedRange) {
        if (state.selectedRange == StatisticsRange.ALL) {
            viewModel.onAction(StatisticsAction.SelectRange(StatisticsRange.LAST_30_DAYS))
        }
    }
    StatisticsChartsScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onRangeSelected = { viewModel.onAction(StatisticsAction.SelectRange(it)) }
    )
}
