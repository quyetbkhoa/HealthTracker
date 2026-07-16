package com.quyetbkhoa.healthtracker.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.Immutable
import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import com.quyetbkhoa.healthtracker.domain.model.Gender
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.domain.usecase.CalculateTdeeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class ProfileSetupUiState(
    val fullName: String = "",
    val dateOfBirth: Long? = null,
    val gender: Gender = Gender.MALE,
    val weightStr: String = "",
    val heightStr: String = "",
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    val goal: Goal = Goal.MAINTAIN,
    val acceptedTerms: Boolean = false,
    val fullNameError: Int? = null,
    val dobError: Int? = null,
    val weightError: Int? = null,
    val heightError: Int? = null,
    val termsError: Int? = null,
    val estimatedBmr: Int = 0,
    val estimatedTdee: Int = 0,
    val estimatedTarget: Int = 0
)

sealed interface ProfileSetupAction {
    data class UpdateFullName(val name: String) : ProfileSetupAction
    data class UpdateDateOfBirth(val dobMillis: Long) : ProfileSetupAction
    data class UpdateGender(val gender: Gender) : ProfileSetupAction
    data class UpdateWeight(val weight: String) : ProfileSetupAction
    data class UpdateHeight(val height: String) : ProfileSetupAction
    data class UpdateActivityLevel(val level: ActivityLevel) : ProfileSetupAction
    data class UpdateGoal(val goal: Goal) : ProfileSetupAction
    data class UpdateAcceptedTerms(val accepted: Boolean) : ProfileSetupAction
    
    data object SubmitInformation : ProfileSetupAction
    data object SubmitProfile : ProfileSetupAction
}

sealed interface ProfileSetupUiEvent {
    object NavigateToStep2 : ProfileSetupUiEvent
    object NavigateToDashboard : ProfileSetupUiEvent
    data class ShowToast(val message: String) : ProfileSetupUiEvent
}

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val calculateTdee: CalculateTdeeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<ProfileSetupUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onAction(action: ProfileSetupAction) {
        when (action) {
            is ProfileSetupAction.UpdateFullName -> {
                _uiState.update { it.copy(fullName = action.name, fullNameError = null) }
            }
            is ProfileSetupAction.UpdateDateOfBirth -> {
                _uiState.update { it.copy(dateOfBirth = action.dobMillis, dobError = null) }
            }
            is ProfileSetupAction.UpdateGender -> {
                _uiState.update { it.copy(gender = action.gender) }
            }
            is ProfileSetupAction.UpdateWeight -> {
                _uiState.update { it.copy(weightStr = action.weight, weightError = null) }
            }
            is ProfileSetupAction.UpdateHeight -> {
                _uiState.update { it.copy(heightStr = action.height, heightError = null) }
            }
            is ProfileSetupAction.UpdateActivityLevel -> {
                _uiState.update { state -> state.copy(activityLevel = action.level).withEstimate() }
            }
            is ProfileSetupAction.UpdateGoal -> {
                _uiState.update { state -> state.copy(goal = action.goal).withEstimate() }
            }
            is ProfileSetupAction.UpdateAcceptedTerms -> {
                _uiState.update { it.copy(acceptedTerms = action.accepted, termsError = null) }
            }
            ProfileSetupAction.SubmitInformation -> validateInformation()
            ProfileSetupAction.SubmitProfile -> submitProfile()
        }
    }

    private fun validateInformation() {
        val state = _uiState.value
        var hasError = false

        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(fullNameError = com.quyetbkhoa.healthtracker.R.string.error_empty_name) }
            hasError = true
        }
        val currentTime = System.currentTimeMillis()
        if (state.dateOfBirth == null || state.dateOfBirth <= 0L || state.dateOfBirth >= currentTime) {
            _uiState.update { it.copy(dobError = com.quyetbkhoa.healthtracker.R.string.error_invalid_dob) }
            hasError = true
        }

        val weight = state.weightStr.toFloatOrNull()
        if (weight == null || weight < 1f || weight > 300f) {
            _uiState.update { it.copy(weightError = com.quyetbkhoa.healthtracker.R.string.error_invalid_weight) }
            hasError = true
        }
        val height = state.heightStr.toFloatOrNull()
        if (height == null || height < 1f || height > 300f) {
            _uiState.update { it.copy(heightError = com.quyetbkhoa.healthtracker.R.string.error_invalid_height) }
            hasError = true
        }

        if (!hasError) {
            _uiState.update { it.withEstimate() }
            viewModelScope.launch {
                _uiEvent.send(ProfileSetupUiEvent.NavigateToStep2)
            }
        }
    }

    private fun submitProfile() {
        val state = _uiState.value
        val profile = UserProfile(
            fullName = state.fullName,
            dateOfBirth = state.dateOfBirth ?: 0L,
            gender = state.gender,
            weightKg = state.weightStr.toFloatOrNull() ?: 0f,
            heightCm = state.heightStr.toFloatOrNull() ?: 0f,
            activityLevel = state.activityLevel,
            goal = state.goal
        )

        val result = calculateTdee(profile)
        viewModelScope.launch {
            profileRepository.saveProfile(
                profile.copy(
                    bmrCalories = result.bmrCalories,
                    tdeeCalories = result.tdeeCalories,
                    dailyCalorieTarget = result.targetCalories
                )
            )
            _uiEvent.send(ProfileSetupUiEvent.NavigateToDashboard)
        }
    }

    private fun ProfileSetupUiState.withEstimate(): ProfileSetupUiState {
        val birthDate = dateOfBirth ?: return this
        val weight = weightStr.toFloatOrNull() ?: return this
        val height = heightStr.toFloatOrNull() ?: return this
        val result = calculateTdee(
            UserProfile(
                dateOfBirth = birthDate,
                gender = gender,
                weightKg = weight,
                heightCm = height,
                activityLevel = activityLevel,
                goal = goal
            )
        )
        return copy(estimatedBmr = result.bmrCalories, estimatedTdee = result.tdeeCalories, estimatedTarget = result.targetCalories)
    }
}
