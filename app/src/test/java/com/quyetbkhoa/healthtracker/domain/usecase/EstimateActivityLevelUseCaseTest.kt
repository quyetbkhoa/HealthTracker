package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EstimateActivityLevelUseCaseTest {
    private val estimate = EstimateActivityLevelUseCase()

    @Test
    fun noActivityRecords_stillIncludesBaselineEnergyBurn() {
        val result = estimate(1_600, totalExerciseCalories = 0.0, trackedDays = 28)
        assertNotNull(result)
        result as ActivityLevelEstimate
        assertEquals(1.2, result.activityFactor, 0.001)
        assertEquals(1_920, result.estimatedTdeeCalories)
        assertEquals(ActivityLevel.SEDENTARY, result.activityLevel)
    }

    @Test
    fun missingDaysCountAsZeroInTheFullWindow() {
        val result = estimate(1_600, totalExerciseCalories = 2_800.0, trackedDays = 28)!!
        assertEquals(100.0, result.averageExerciseCalories, 0.001)
        assertEquals(2_020, result.estimatedTdeeCalories)
    }
}
