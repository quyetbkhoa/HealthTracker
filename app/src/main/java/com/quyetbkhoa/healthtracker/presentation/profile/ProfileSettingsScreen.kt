package com.quyetbkhoa.healthtracker.presentation.profile

import androidx.compose.runtime.Composable

@Composable
fun ProfileSettingsScreen(
    state: ProfileSettingsUiState,
    onAction: (ProfileSettingsAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    ProfileSettingsContent(state, onAction, onNavigateBack)
}
