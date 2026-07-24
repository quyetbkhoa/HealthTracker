package com.quyetbkhoa.healthtracker.presentation.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DashboardRoute(
    onNavigateToSettings: () -> Unit,
    onNavigateToAddMeal: () -> Unit,
    onNavigateToAddActivity: () -> Unit,
    onNavigateToActivityHistory: () -> Unit,
    onNavigateToMealJournal: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                DashboardUiEvent.NavigateToAddMeal -> onNavigateToAddMeal()
                DashboardUiEvent.NavigateToAddActivity -> onNavigateToAddActivity()
                DashboardUiEvent.NavigateToMealJournal -> onNavigateToMealJournal()
            }
        }
    }
    DashboardScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToActivityHistory = onNavigateToActivityHistory,
        onNavigateToStatistics = onNavigateToStatistics
    )
}
