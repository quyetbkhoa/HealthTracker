package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import javax.inject.Inject

data class ActivityLevelEstimate(
    val averageExerciseCalories: Double,
    val activityFactor: Double,
    val activityLevel: ActivityLevel,
    val estimatedTdeeCalories: Int
)

/** Missing activity records are intentionally treated as zero-calorie days. */
class EstimateActivityLevelUseCase @Inject constructor() {
    operator fun invoke(
        bmrCalories: Int,
        totalExerciseCalories: Double,
        trackedDays: Int
    ): ActivityLevelEstimate? {
        if (bmrCalories <= 0 || trackedDays <= 0) return null
        val days = trackedDays.coerceAtMost(28)
        val average = totalExerciseCalories.coerceAtLeast(0.0) / days
        val factor = (1.2 + average / bmrCalories).coerceIn(1.2, 1.9)
        val level = when {
            factor < 1.2875 -> ActivityLevel.SEDENTARY
            factor < 1.4625 -> ActivityLevel.LIGHT
            factor < 1.6375 -> ActivityLevel.MODERATE
            factor < 1.8125 -> ActivityLevel.VERY_ACTIVE
            else -> ActivityLevel.EXTRA_ACTIVE
        }
        return ActivityLevelEstimate(average, factor, level, (bmrCalories * factor).toInt())
    }
}
