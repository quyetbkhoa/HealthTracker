package com.quyetbkhoa.healthtracker.presentation.activity

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

@Composable
fun AddActivityScreen(
    state: AddActivityUiState,
    snackbar: SnackbarHostState,
    onAction: (AddActivityAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    AddActivityContent(state, snackbar, onAction, onNavigateBack)
}
