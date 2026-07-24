package com.quyetbkhoa.healthtracker.presentation.mealjournal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.domain.model.MealType

@Composable
fun MealJournalRoute(
    onNavigateBack: () -> Unit,
    onAddMeal: (Long, MealType) -> Unit,
    viewModel: MealJournalViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MealJournalScreen(state, viewModel::onAction, onNavigateBack, onAddMeal)
}
