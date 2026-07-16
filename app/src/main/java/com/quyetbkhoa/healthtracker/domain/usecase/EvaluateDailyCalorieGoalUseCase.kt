package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.Goal
import javax.inject.Inject
import kotlin.math.roundToInt

enum class DailyCalorieStatus {
    NEEDS_MORE,
    GOOD,
    EXCEEDED
}

data class DailyCalorieEvaluation(
    val status: DailyCalorieStatus,
    val lowerBound: Int,
    val upperBound: Int,
    val caloriesToBoundary: Int
)

class EvaluateDailyCalorieGoalUseCase @Inject constructor() {
    operator fun invoke(consumedCalories: Int, targetCalories: Int, goal: Goal): DailyCalorieEvaluation {
        if (targetCalories <= 0) {
            return DailyCalorieEvaluation(DailyCalorieStatus.NEEDS_MORE, 0, 0, 0)
        }

        val (lowerBound, upperBound) = when (goal) {
            Goal.LOSE_WEIGHT -> (targetCalories * 0.75).roundToInt() to targetCalories
            Goal.MAINTAIN -> {
                val tolerance = (targetCalories * 0.10).roundToInt().coerceAtMost(200)
                targetCalories - tolerance to targetCalories + tolerance
            }
            Goal.GAIN_WEIGHT -> targetCalories to (targetCalories * 1.10).roundToInt()
        }

        return when {
            consumedCalories < lowerBound -> DailyCalorieEvaluation(
                DailyCalorieStatus.NEEDS_MORE,
                lowerBound,
                upperBound,
                lowerBound - consumedCalories
            )
            consumedCalories > upperBound -> DailyCalorieEvaluation(
                DailyCalorieStatus.EXCEEDED,
                lowerBound,
                upperBound,
                consumedCalories - upperBound
            )
            else -> DailyCalorieEvaluation(DailyCalorieStatus.GOOD, lowerBound, upperBound, 0)
        }
    }
}
