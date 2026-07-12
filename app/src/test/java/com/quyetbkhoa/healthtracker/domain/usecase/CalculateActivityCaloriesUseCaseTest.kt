package com.quyetbkhoa.healthtracker.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateActivityCaloriesUseCaseTest {
    private val useCase = CalculateActivityCaloriesUseCase()

    @Test
    fun `calculates calories for thirty minutes`() {
        assertEquals(120.0, useCase(met = 4.0, weightKg = 60.0, durationMinutes = 30), 0.0001)
    }

    @Test
    fun `calculates calories for sixty minutes`() {
        assertEquals(280.0, useCase(met = 4.0, weightKg = 70.0, durationMinutes = 60), 0.0001)
    }

    @Test
    fun `keeps precision when duration is not whole hour`() {
        assertEquals(181.675, useCase(met = 4.3, weightKg = 65.0, durationMinutes = 39), 0.0001)
    }

    @Test
    fun `returns zero for invalid inputs`() {
        assertEquals(0.0, useCase(4.0, 60.0, 0), 0.0)
        assertEquals(0.0, useCase(4.0, 0.0, 30), 0.0)
        assertEquals(0.0, useCase(0.0, 60.0, 30), 0.0)
        assertEquals(0.0, useCase(-1.0, 60.0, 30), 0.0)
        assertEquals(0.0, useCase(4.0, -1.0, 30), 0.0)
        assertEquals(0.0, useCase(4.0, 60.0, -1), 0.0)
        assertEquals(0.0, useCase(4.0, 60.0, 601), 0.0)
    }
}
