package com.quyetbkhoa.healthtracker.presentation.dashboard

import androidx.compose.runtime.Composable

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToActivityHistory: () -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    DashboardContent(
        uiState = uiState,
        onAction = onAction,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToActivityHistory = onNavigateToActivityHistory,
        onNavigateToStatistics = onNavigateToStatistics
    )
}
