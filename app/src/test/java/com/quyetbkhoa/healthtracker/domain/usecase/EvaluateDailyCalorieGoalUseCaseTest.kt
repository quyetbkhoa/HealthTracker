package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.Goal
import org.junit.Assert.assertEquals
import org.junit.Test

class EvaluateDailyCalorieGoalUseCaseTest {
    private val evaluate = EvaluateDailyCalorieGoalUseCase()

    @Test fun loseWeight_acceptsSeventyFiveThroughOneHundredPercent() {
        assertEquals(DailyCalorieStatus.NEEDS_MORE, evaluate(1_499, 2_000, Goal.LOSE_WEIGHT).status)
        assertEquals(DailyCalorieStatus.GOOD, evaluate(1_500, 2_000, Goal.LOSE_WEIGHT).status)
        assertEquals(DailyCalorieStatus.GOOD, evaluate(2_000, 2_000, Goal.LOSE_WEIGHT).status)
        assertEquals(DailyCalorieStatus.EXCEEDED, evaluate(2_001, 2_000, Goal.LOSE_WEIGHT).status)
    }

    @Test fun maintainUsesTenPercentCappedAtTwoHundredCalories() {
        val evaluation = evaluate(2_200, 2_000, Goal.MAINTAIN)
        assertEquals(1_800, evaluation.lowerBound)
        assertEquals(2_200, evaluation.upperBound)
        assertEquals(DailyCalorieStatus.GOOD, evaluation.status)
    }

    @Test fun gainWeightAcceptsTargetThroughOneHundredTenPercent() {
        assertEquals(DailyCalorieStatus.NEEDS_MORE, evaluate(1_999, 2_000, Goal.GAIN_WEIGHT).status)
        assertEquals(DailyCalorieStatus.GOOD, evaluate(2_200, 2_000, Goal.GAIN_WEIGHT).status)
        assertEquals(DailyCalorieStatus.EXCEEDED, evaluate(2_201, 2_000, Goal.GAIN_WEIGHT).status)
    }
}
