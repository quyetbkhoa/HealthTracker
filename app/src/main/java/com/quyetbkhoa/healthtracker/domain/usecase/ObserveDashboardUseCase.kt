package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class DashboardData(
    val profile: UserProfile?,
    val meals: List<MealEntry>,
    val consumedCalories: Int,
    val exerciseCalories: Int,
    val suggestedActivityLevel: ActivityLevel?,
    val suggestedTdeeCalories: Int,
    val calorieEvaluation: DailyCalorieEvaluation
)

class ObserveDashboardUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val mealRepository: MealRepository,
    private val activityRepository: ActivityRepository,
    private val evaluateDailyCalorieGoal: EvaluateDailyCalorieGoalUseCase,
    private val estimateActivityLevel: EstimateActivityLevelUseCase,
    private val clock: Clock
) {
    operator fun invoke(languageTag: String): Flow<DashboardData> {
        val today = LocalDate.now(clock)
        val zone = clock.zone
        val activityWindowStart = today.minusDays(ACTIVITY_WINDOW_DAYS - 1L)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val activityWindowEnd = today.plusDays(1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        return combine(
            profileRepository.userProfile,
            mealRepository.observeMealsByDay(today.toEpochDay(), languageTag),
            activityRepository.observeTotalCaloriesByDay(today.toEpochDay()),
            activityRepository.observeTotalCaloriesBetween(activityWindowStart, activityWindowEnd)
        ) { profile, meals, activityCalories, windowActivityCalories ->
            val consumedCalories = meals.sumOf(MealEntry::calories)
            val targetCalories = profile?.dailyCalorieTarget ?: 0
            val trackingDays = profile.trackingDays(today)
            val estimate = profile?.let {
                estimateActivityLevel(it.bmrCalories, windowActivityCalories, trackingDays)
            }
            val shouldSuggest = profile != null && trackingDays >= MIN_TRACKING_DAYS_FOR_SUGGESTION &&
                estimate != null && estimate.activityLevel != profile.activityLevel &&
                kotlin.math.abs(estimate.estimatedTdeeCalories - profile.tdeeCalories) >=
                MIN_TDEE_DIFFERENCE_FOR_SUGGESTION

            DashboardData(
                profile = profile,
                meals = meals,
                consumedCalories = consumedCalories,
                exerciseCalories = activityCalories.toInt(),
                suggestedActivityLevel = estimate?.activityLevel.takeIf { shouldSuggest },
                suggestedTdeeCalories = estimate?.estimatedTdeeCalories.takeIf { shouldSuggest } ?: 0,
                calorieEvaluation = evaluateDailyCalorieGoal(
                    consumedCalories,
                    targetCalories,
                    profile?.goal ?: Goal.MAINTAIN
                )
            )
        }
    }

    private fun UserProfile?.trackingDays(today: LocalDate): Int = this
        ?.activityTrackingStartedAt
        ?.takeIf { it > 0L }
        ?.let { startedAt ->
            val startDate = Instant.ofEpochMilli(startedAt).atZone(clock.zone).toLocalDate()
            (ChronoUnit.DAYS.between(startDate, today).toInt() + 1)
                .coerceIn(1, ACTIVITY_WINDOW_DAYS)
        } ?: 0

    private companion object {
        const val ACTIVITY_WINDOW_DAYS = 28
        const val MIN_TRACKING_DAYS_FOR_SUGGESTION = 14
        const val MIN_TDEE_DIFFERENCE_FOR_SUGGESTION = 150
    }
}
