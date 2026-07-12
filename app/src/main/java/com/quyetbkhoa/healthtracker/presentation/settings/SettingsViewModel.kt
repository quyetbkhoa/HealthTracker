package com.quyetbkhoa.healthtracker.presentation.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.domain.usecase.ResetUserDataUseCase
import com.quyetbkhoa.healthtracker.domain.model.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class SettingsUiState(
    val showResetConfirmation: Boolean = false,
    val isResetting: Boolean = false
)

sealed interface SettingsAction {
    data class SelectLanguage(val language: AppLanguage) : SettingsAction
    data object RequestReset : SettingsAction
    data object CancelReset : SettingsAction
    data object ConfirmReset : SettingsAction
}

sealed interface SettingsUiEvent {
    data class LanguageSelected(val language: AppLanguage) : SettingsUiEvent
    data object ResetCompleted : SettingsUiEvent
    data object ResetFailed : SettingsUiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val resetUserDataUseCase: ResetUserDataUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<SettingsUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SelectLanguage -> selectLanguage(action.language)
            SettingsAction.RequestReset -> _uiState.update {
                it.copy(showResetConfirmation = true)
            }
            SettingsAction.CancelReset -> _uiState.update {
                it.copy(showResetConfirmation = false)
            }
            SettingsAction.ConfirmReset -> resetData()
        }
    }

    private fun selectLanguage(language: AppLanguage) {
        viewModelScope.launch {
            _uiEvent.send(SettingsUiEvent.LanguageSelected(language))
        }
    }

    private fun resetData() {
        if (_uiState.value.isResetting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResetting = true) }
            try {
                resetUserDataUseCase()
                _uiState.update { it.copy(showResetConfirmation = false) }
                _uiEvent.send(SettingsUiEvent.ResetCompleted)
            } catch (_: Exception) {
                _uiEvent.send(SettingsUiEvent.ResetFailed)
            } finally {
                _uiState.update { it.copy(isResetting = false) }
            }
        }
    }
}
