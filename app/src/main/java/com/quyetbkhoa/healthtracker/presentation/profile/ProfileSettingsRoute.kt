package com.quyetbkhoa.healthtracker.presentation.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfileSettingsRoute(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ProfileSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            if (event == ProfileSettingsEvent.Saved) {
                onSaved()
            }
        }
    }
    ProfileSettingsScreen(state, viewModel::onAction, onNavigateBack)
}
