package com.quyetbkhoa.healthtracker.presentation.activity

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AddActivityRoute(
    epochDay: Long,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddActivityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val errorMessage = state.error?.let { addActivityErrorText(it) }

    LaunchedEffect(epochDay) {
        viewModel.onAction(AddActivityAction.SetDate(epochDay))
    }
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { onSaved() }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbar.showSnackbar(it) }
    }

    AddActivityScreen(state, snackbar, viewModel::onAction, onNavigateBack)
}
