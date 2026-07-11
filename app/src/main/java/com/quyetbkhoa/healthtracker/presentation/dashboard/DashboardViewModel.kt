package com.quyetbkhoa.healthtracker.presentation.dashboard

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.domain.repository.DailyCalorieRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

@HiltViewModel
class DashboardViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    dailyCalorieRepository: DailyCalorieRepository
) : ViewModel() {
    private val todayEpochDay = LocalDate.now().toEpochDay()

    val uiState: StateFlow<DashboardUiState> = combine(
        profileRepository.userProfile,
        dailyCalorieRepository.observeSummary(todayEpochDay)
    ) { profile, daily ->
        DashboardUiState(
            isLoading = false,
            hasProfile = profile != null,
            tdeeCalories = profile?.tdeeCalories ?: 0,
            targetCalories = profile?.dailyCalorieTarget ?: 0,
            consumedCalories = daily.consumedCalories,
            exerciseCalories = daily.exerciseCalories,
            userName = profile?.fullName.orEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.AddMeal -> Unit
            DashboardAction.AddActivity -> Unit
        }
    }
}
