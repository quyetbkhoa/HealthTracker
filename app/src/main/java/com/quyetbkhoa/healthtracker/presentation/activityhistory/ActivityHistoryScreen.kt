package com.quyetbkhoa.healthtracker.presentation.activityhistory

import androidx.compose.runtime.Composable

@Composable
fun ActivityHistoryScreen(
    state: ActivityHistoryUiState,
    onAction: (ActivityHistoryAction) -> Unit,
    onNavigateBack: () -> Unit,
    onAddActivity: () -> Unit
) {
    ActivityHistoryContent(state, onAction, onNavigateBack, onAddActivity)
}
