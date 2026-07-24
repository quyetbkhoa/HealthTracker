package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.Goal
import org.junit.Assert.assertEquals
import org.junit.Test

class EvaluateDailyCalorieGoalUseCaseTest {
    private val evaluate = EvaluateDailyCalorieGoalUseCase()

    @Test fun loseWeight_acceptsNinetyFiveThroughOneHundredPercent() {
        assertEquals(DailyCalorieStatus.NEEDS_MORE, evaluate(1_899, 2_000, Goal.LOSE_WEIGHT).status)
        assertEquals(DailyCalorieStatus.GOOD, evaluate(1_900, 2_000, Goal.LOSE_WEIGHT).status)
        assertEquals(DailyCalorieStatus.GOOD, evaluate(2_000, 2_000, Goal.LOSE_WEIGHT).status)
        assertEquals(DailyCalorieStatus.EXCEEDED, evaluate(2_001, 2_000, Goal.LOSE_WEIGHT).status)
    }

    @Test fun maintainUsesFivePercentTolerance() {
        val evaluation = evaluate(2_100, 2_000, Goal.MAINTAIN)
        assertEquals(1_900, evaluation.lowerBound)
        assertEquals(2_100, evaluation.upperBound)
        assertEquals(DailyCalorieStatus.GOOD, evaluation.status)
        assertEquals(DailyCalorieStatus.NEEDS_MORE, evaluate(1_899, 2_000, Goal.MAINTAIN).status)
        assertEquals(DailyCalorieStatus.EXCEEDED, evaluate(2_101, 2_000, Goal.MAINTAIN).status)
    }

    @Test fun gainWeightAcceptsTargetThroughOneHundredFivePercent() {
        assertEquals(DailyCalorieStatus.NEEDS_MORE, evaluate(1_999, 2_000, Goal.GAIN_WEIGHT).status)
        assertEquals(DailyCalorieStatus.GOOD, evaluate(2_100, 2_000, Goal.GAIN_WEIGHT).status)
        assertEquals(DailyCalorieStatus.EXCEEDED, evaluate(2_101, 2_000, Goal.GAIN_WEIGHT).status)
    }
}
