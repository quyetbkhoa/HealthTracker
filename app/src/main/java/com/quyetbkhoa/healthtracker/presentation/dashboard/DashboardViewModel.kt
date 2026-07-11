package com.quyetbkhoa.healthtracker.presentation.dashboard

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.domain.repository.DailyCalorieRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@Immutable
data class DashboardUiState(
    val isLoading: Boolean = true,
    val hasProfile: Boolean = false,
    val tdeeCalories: Int = 0,
    val targetCalories: Int = 0,
    val consumedCalories: Int = 0,
    val exerciseCalories: Int = 0,
    val userName: String = ""
) {
    val allowedCalories: Int get() = targetCalories + exerciseCalories
    val remainingCalories: Int get() = allowedCalories - consumedCalories
    val isExceeded: Boolean get() = remainingCalories < 0
    val progress: Float get() = if (allowedCalories > 0) consumedCalories.toFloat() / allowedCalories else 0f
    val progressPercent: Int get() = (progress * 100).toInt()
    val waterLiters: Float get() = 0f
    val waterGoalLiters: Float get() = 2.5f
    val waterProgress: Float get() = waterLiters / waterGoalLiters
}

sealed interface DashboardAction {
    data object AddMeal : DashboardAction
    data object AddActivity : DashboardAction
}

sealed interface DashboardUiEvent {
    data object NavigateToAddMeal : DashboardUiEvent
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    dailyCalorieRepository: DailyCalorieRepository,
    mealRepository: MealRepository
) : ViewModel() {
    private val todayEpochDay = LocalDate.now().toEpochDay()

    private val _uiEvent = Channel<DashboardUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        profileRepository.userProfile,
        dailyCalorieRepository.observeSummary(todayEpochDay),
        mealRepository.observeMealsByDay(todayEpochDay)
    ) { profile, daily, meals ->
        DashboardUiState(
            isLoading = false,
            hasProfile = profile != null,
            tdeeCalories = profile?.tdeeCalories ?: 0,
            targetCalories = profile?.dailyCalorieTarget ?: 0,
            consumedCalories = meals.sumOf { it.calories },
            exerciseCalories = daily.exerciseCalories,
            userName = profile?.fullName.orEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.AddMeal -> viewModelScope.launch {
                _uiEvent.send(DashboardUiEvent.NavigateToAddMeal)
            }
            DashboardAction.AddActivity -> Unit
        }
    }
}
