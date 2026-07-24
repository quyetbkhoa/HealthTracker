package com.quyetbkhoa.healthtracker.presentation.meal

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R

@Composable
fun AddMealRoute(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddMealViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val failedMessage = stringResource(R.string.add_meal_save_failed)
    val favoriteFailedMessage = stringResource(R.string.add_meal_favorite_failed)

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                AddMealUiEvent.Saved -> onSaved()
                AddMealUiEvent.SaveFailed -> snackbar.showSnackbar(failedMessage)
                AddMealUiEvent.FavoriteFailed -> snackbar.showSnackbar(favoriteFailedMessage)
            }
        }
    }

    AddMealScreen(state, snackbar, viewModel::onAction, onNavigateBack)
}
