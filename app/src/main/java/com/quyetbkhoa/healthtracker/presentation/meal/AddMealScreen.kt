package com.quyetbkhoa.healthtracker.presentation.meal

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

@Composable
fun AddMealScreen(
    state: AddMealUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (AddMealAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    AddMealContent(state, snackbarHostState, onAction, onNavigateBack)
}
