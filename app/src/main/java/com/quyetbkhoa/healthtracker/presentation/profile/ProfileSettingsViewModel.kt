package com.quyetbkhoa.healthtracker.presentation.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import com.quyetbkhoa.healthtracker.domain.model.Gender
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.domain.usecase.BmiCategory
import com.quyetbkhoa.healthtracker.domain.usecase.CalculateBmiUseCase
import com.quyetbkhoa.healthtracker.domain.usecase.SaveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class ProfileSettingsUiState(
    val isLoading: Boolean = true,
    val fullName: String = "",
    val dateOfBirth: Long? = null,
    val gender: Gender = Gender.MALE,
    val weightInput: String = "",
    val heightInput: String = "",
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    val goal: Goal = Goal.MAINTAIN,
    val bmi: Float? = null,
    val bmiCategory: BmiCategory? = null,
    val fullNameError: Int? = null,
    val dobError: Int? = null,
    val weightError: Int? = null,
    val heightError: Int? = null
)

sealed interface ProfileSettingsAction {
    data class UpdateFullName(val value: String) : ProfileSettingsAction
    data class UpdateDateOfBirth(val value: Long) : ProfileSettingsAction
    data class UpdateGender(val value: Gender) : ProfileSettingsAction
    data class UpdateWeight(val value: String) : ProfileSettingsAction
    data class UpdateHeight(val value: String) : ProfileSettingsAction
    data class UpdateActivityLevel(val value: ActivityLevel) : ProfileSettingsAction
    data class UpdateGoal(val value: Goal) : ProfileSettingsAction
    data object Save : ProfileSettingsAction
}

sealed interface ProfileSettingsEvent {
    data object Saved : ProfileSettingsEvent
}

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val saveProfile: SaveProfileUseCase,
    private val calculateBmi: CalculateBmiUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileSettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<ProfileSettingsEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()
    private var loadedProfile: UserProfile? = null

    init {
        viewModelScope.launch {
            profileRepository.userProfile.first()?.let(::loadProfile)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onAction(action: ProfileSettingsAction) {
        when (action) {
            is ProfileSettingsAction.UpdateFullName -> _uiState.update { it.copy(fullName = action.value, fullNameError = null) }
            is ProfileSettingsAction.UpdateDateOfBirth -> _uiState.update { it.copy(dateOfBirth = action.value, dobError = null) }
            is ProfileSettingsAction.UpdateGender -> _uiState.update { it.copy(gender = action.value) }
            is ProfileSettingsAction.UpdateWeight -> updateBodyValues(weight = action.value)
            is ProfileSettingsAction.UpdateHeight -> updateBodyValues(height = action.value)
            is ProfileSettingsAction.UpdateActivityLevel -> _uiState.update { it.copy(activityLevel = action.value) }
            is ProfileSettingsAction.UpdateGoal -> _uiState.update { it.copy(goal = action.value) }
            ProfileSettingsAction.Save -> save()
        }
    }

    private fun loadProfile(profile: UserProfile) {
        loadedProfile = profile
        val bmi = calculateBmi(profile.weightKg, profile.heightCm)
        _uiState.value = ProfileSettingsUiState(
            isLoading = false,
            fullName = profile.fullName,
            dateOfBirth = profile.dateOfBirth,
            gender = profile.gender,
            weightInput = profile.weightKg.toString(),
            heightInput = profile.heightCm.toString(),
            activityLevel = profile.activityLevel,
            goal = profile.goal,
            bmi = bmi?.value,
            bmiCategory = bmi?.category
        )
    }

    private fun updateBodyValues(weight: String? = null, height: String? = null) {
        _uiState.update { current ->
            val weightInput = weight?.filter { it.isDigit() || it == '.' } ?: current.weightInput
            val heightInput = height?.filter { it.isDigit() || it == '.' } ?: current.heightInput
            val bmi = calculateBmi(weightInput.toFloatOrNull() ?: 0f, heightInput.toFloatOrNull() ?: 0f)
            current.copy(
                weightInput = weightInput,
                heightInput = heightInput,
                weightError = if (weight != null) null else current.weightError,
                heightError = if (height != null) null else current.heightError,
                bmi = bmi?.value,
                bmiCategory = bmi?.category
            )
        }
    }

    private fun save() {
        val state = _uiState.value
        val weight = state.weightInput.toFloatOrNull()
        val height = state.heightInput.toFloatOrNull()
        val invalidName = state.fullName.isBlank()
        val invalidDob = state.dateOfBirth == null || state.dateOfBirth <= 0L || state.dateOfBirth >= System.currentTimeMillis()
        val invalidWeight = weight == null || weight !in 1f..300f
        val invalidHeight = height == null || height !in 1f..300f
        if (invalidName || invalidDob || invalidWeight || invalidHeight) {
            _uiState.update {
                it.copy(
                    fullNameError = if (invalidName) R.string.error_empty_name else null,
                    dobError = if (invalidDob) R.string.error_invalid_dob else null,
                    weightError = if (invalidWeight) R.string.error_invalid_weight else null,
                    heightError = if (invalidHeight) R.string.error_invalid_height else null
                )
            }
            return
        }

        val baseProfile = UserProfile(
            fullName = state.fullName.trim(),
            dateOfBirth = state.dateOfBirth ?: return,
            gender = state.gender,
            weightKg = weight ?: return,
            heightCm = height ?: return,
            activityLevel = state.activityLevel,
            goal = state.goal
        )
        viewModelScope.launch {
            saveProfile(
                baseProfile.copy(
                    activityTrackingStartedAt = loadedProfile?.activityTrackingStartedAt ?: 0L
                )
            )
            _event.send(ProfileSettingsEvent.Saved)
        }
    }
}
