package com.quyetbkhoa.healthtracker.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculateBmiUseCaseTest {
    private val calculateBmi = CalculateBmiUseCase()

    @Test
    fun invalidBodyValues_returnNull() {
        assertNull(calculateBmi(0f, 170f))
        assertNull(calculateBmi(60f, 0f))
    }

    @Test
    fun bmiCategories_followProjectThresholds() {
        assertEquals(BmiCategory.UNDERWEIGHT, calculateBmi(50f, 175f)?.category)
        assertEquals(BmiCategory.NORMAL, calculateBmi(65f, 170f)?.category)
        assertEquals(BmiCategory.OVERWEIGHT, calculateBmi(80f, 170f)?.category)
        assertEquals(BmiCategory.OBESE, calculateBmi(100f, 170f)?.category)
    }
}
