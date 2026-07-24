package com.quyetbkhoa.healthtracker.presentation.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.data.seed.DemoDataInitializer
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
    val isResetting: Boolean = false,
    val showDemoDataConfirmation: Boolean = false,
    val isLoadingDemoData: Boolean = false
)

sealed interface SettingsAction {
    data class SelectLanguage(val language: AppLanguage) : SettingsAction
    data object RequestReset : SettingsAction
    data object CancelReset : SettingsAction
    data object ConfirmReset : SettingsAction
    data object RequestDemoData : SettingsAction
    data object CancelDemoData : SettingsAction
    data object ConfirmDemoData : SettingsAction
}

sealed interface SettingsUiEvent {
    data class LanguageSelected(val language: AppLanguage) : SettingsUiEvent
    data object ResetCompleted : SettingsUiEvent
    data object ResetFailed : SettingsUiEvent
    data object DemoDataLoaded : SettingsUiEvent
    data object DemoDataFailed : SettingsUiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val resetUserDataUseCase: ResetUserDataUseCase,
    private val demoDataInitializer: DemoDataInitializer
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<SettingsUiEvent>(Channel.BUFFERED)
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
            SettingsAction.RequestDemoData -> _uiState.update {
                it.copy(showDemoDataConfirmation = true)
            }
            SettingsAction.CancelDemoData -> _uiState.update {
                it.copy(showDemoDataConfirmation = false)
            }
            SettingsAction.ConfirmDemoData -> loadDemoData()
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

    private fun loadDemoData() {
        if (_uiState.value.isLoadingDemoData) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDemoData = true) }
            try {
                demoDataInitializer.replaceWithTwoMonthDemo()
                _uiState.update { it.copy(showDemoDataConfirmation = false) }
                _uiEvent.send(SettingsUiEvent.DemoDataLoaded)
            } catch (_: Exception) {
                _uiEvent.send(SettingsUiEvent.DemoDataFailed)
            } finally {
                _uiState.update { it.copy(isLoadingDemoData = false) }
            }
        }
    }
}
