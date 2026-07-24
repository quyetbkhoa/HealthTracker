package com.quyetbkhoa.healthtracker.presentation.mealjournal

import androidx.compose.runtime.Composable
import com.quyetbkhoa.healthtracker.domain.model.MealType

@Composable
fun MealJournalScreen(
    state: MealJournalUiState,
    onAction: (MealJournalAction) -> Unit,
    onNavigateBack: () -> Unit,
    onAddMeal: (Long, MealType) -> Unit
) {
    MealJournalContent(state, onAction, onNavigateBack, onAddMeal)
}
