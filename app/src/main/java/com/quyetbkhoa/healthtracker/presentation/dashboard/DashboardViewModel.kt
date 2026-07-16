package com.quyetbkhoa.healthtracker.presentation.dashboard

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieEvaluation
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieStatus
import com.quyetbkhoa.healthtracker.domain.usecase.EvaluateDailyCalorieGoalUseCase
import com.quyetbkhoa.healthtracker.domain.usecase.EstimateActivityLevelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

@Immutable
data class DashboardUiState(
    val isLoading: Boolean = true,
    val hasProfile: Boolean = false,
    val tdeeCalories: Int = 0,
    val targetCalories: Int = 0,
    val consumedCalories: Int = 0,
    val exerciseCalories: Int = 0,
    val goal: Goal = Goal.MAINTAIN,
    val suggestedActivityLevel: ActivityLevel? = null,
    val suggestedTdeeCalories: Int = 0,
    val calorieEvaluation: DailyCalorieEvaluation = DailyCalorieEvaluation(
        status = DailyCalorieStatus.NEEDS_MORE,
        lowerBound = 0,
        upperBound = 0,
        caloriesToBoundary = 0
    ),
    val userName: String = "",
    val meals: List<MealEntry> = emptyList()
) {
    val allowedCalories: Int get() = targetCalories
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
    data object ViewMeals : DashboardAction
}

sealed interface DashboardUiEvent {
    data object NavigateToAddMeal : DashboardUiEvent
    data object NavigateToAddActivity : DashboardUiEvent
    data object NavigateToMealJournal : DashboardUiEvent
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    mealRepository: MealRepository,
    activityRepository: ActivityRepository,
    evaluateDailyCalorieGoal: EvaluateDailyCalorieGoalUseCase,
    estimateActivityLevel: EstimateActivityLevelUseCase
) : ViewModel() {
    private val todayEpochDay = LocalDate.now().toEpochDay()
    private val zone = ZoneId.systemDefault()
    private val activityWindowStart = LocalDate.now().minusDays(27).atStartOfDay(zone).toInstant().toEpochMilli()
    private val activityWindowEnd = LocalDate.now().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    private val languageTag = Locale.getDefault().language.takeIf { it == "en" } ?: "vi"

    private val _uiEvent = Channel<DashboardUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        profileRepository.userProfile,
        mealRepository.observeMealsByDay(todayEpochDay, languageTag),
        activityRepository.observeTotalCaloriesByDay(todayEpochDay),
        activityRepository.observeTotalCaloriesBetween(activityWindowStart, activityWindowEnd)
    ) { profile, meals, activityCalories, windowActivityCalories ->
        val targetCalories = profile?.dailyCalorieTarget ?: 0
        val consumedCalories = meals.sumOf { it.calories }
        val goal = profile?.goal ?: Goal.MAINTAIN
        val trackingDays = profile?.activityTrackingStartedAt
            ?.takeIf { it > 0L }
            ?.let { startedAt ->
                val startDate = Instant.ofEpochMilli(startedAt).atZone(zone).toLocalDate()
                (ChronoUnit.DAYS.between(startDate, LocalDate.now()).toInt() + 1).coerceIn(1, 28)
            } ?: 0
        val activityEstimate = profile?.let {
            estimateActivityLevel(it.bmrCalories, windowActivityCalories, trackingDays)
        }
        val shouldSuggestActivityChange = profile != null && trackingDays >= 14 &&
            activityEstimate != null && activityEstimate.activityLevel != profile.activityLevel &&
            kotlin.math.abs(activityEstimate.estimatedTdeeCalories - profile.tdeeCalories) >= 150
        DashboardUiState(
            isLoading = false,
            hasProfile = profile != null,
            tdeeCalories = profile?.tdeeCalories ?: 0,
            targetCalories = targetCalories,
            consumedCalories = consumedCalories,
            exerciseCalories = activityCalories.toInt(),
            goal = goal,
            suggestedActivityLevel = activityEstimate?.activityLevel.takeIf { shouldSuggestActivityChange },
            suggestedTdeeCalories = activityEstimate?.estimatedTdeeCalories?.takeIf { shouldSuggestActivityChange } ?: 0,
            calorieEvaluation = evaluateDailyCalorieGoal(consumedCalories, targetCalories, goal),
            userName = profile?.fullName.orEmpty(),
            meals = meals
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.AddMeal -> viewModelScope.launch {
                _uiEvent.send(DashboardUiEvent.NavigateToAddMeal)
            }
            DashboardAction.AddActivity -> viewModelScope.launch {
                _uiEvent.send(DashboardUiEvent.NavigateToAddActivity)
            }
            DashboardAction.ViewMeals -> viewModelScope.launch {
                _uiEvent.send(DashboardUiEvent.NavigateToMealJournal)
            }
        }
    }
}
