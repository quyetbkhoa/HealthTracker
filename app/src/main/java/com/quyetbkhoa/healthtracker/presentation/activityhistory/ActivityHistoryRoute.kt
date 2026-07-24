package com.quyetbkhoa.healthtracker.presentation.activityhistory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ActivityHistoryRoute(
    onNavigateBack: () -> Unit,
    onAddActivity: (Long) -> Unit,
    viewModel: ActivityHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ActivityHistoryScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onAddActivity = { onAddActivity(state.selectedEpochDay) }
    )
}
