package com.quyetbkhoa.healthtracker.presentation.tdee

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.domain.usecase.CalculateTdeeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TdeeResultUiState {
    data object Loading : TdeeResultUiState

    @Immutable
    data class Success(
        val profile: UserProfile,
        val bmrCalories: Int,
        val tdeeCalories: Int,
        val targetInput: String,
        val targetError: Boolean = false
    ) : TdeeResultUiState
}

sealed interface TdeeResultAction {
    data class UpdateTarget(val value: String) : TdeeResultAction
    data object Save : TdeeResultAction
}

sealed interface TdeeResultEvent {
    data object NavigateToDashboard : TdeeResultEvent
}

@HiltViewModel
class TdeeResultViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    calculateTdee: CalculateTdeeUseCase
) : ViewModel() {
    private val targetOverride = MutableStateFlow<String?>(null)
    private val hasError = MutableStateFlow(false)

    val uiState: StateFlow<TdeeResultUiState> = combine(
        profileRepository.userProfile,
        targetOverride,
        hasError
    ) { profile, override, error ->
        if (profile == null) return@combine TdeeResultUiState.Loading
        val result = calculateTdee(profile)
        TdeeResultUiState.Success(
            profile = profile,
            bmrCalories = result.bmrCalories,
            tdeeCalories = result.tdeeCalories,
            targetInput = override ?: result.targetCalories.toString(),
            targetError = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TdeeResultUiState.Loading)

    private val _event = Channel<TdeeResultEvent>()
    val event = _event.receiveAsFlow()

    fun onAction(action: TdeeResultAction) {
        when (action) {
            is TdeeResultAction.UpdateTarget -> {
                targetOverride.value = action.value.filter(Char::isDigit)
                hasError.value = false
            }
            TdeeResultAction.Save -> save()
        }
    }

    private fun save() {
        val state = uiState.value as? TdeeResultUiState.Success ?: return
        val target = state.targetInput.toIntOrNull()
        if (target == null || target !in 500..10_000) {
            hasError.value = true
            return
        }
        viewModelScope.launch {
            profileRepository.saveProfile(
                state.profile.copy(
                    bmrCalories = state.bmrCalories,
                    tdeeCalories = state.tdeeCalories,
                    dailyCalorieTarget = target
                )
            )
            _event.send(TdeeResultEvent.NavigateToDashboard)
        }
    }
}
