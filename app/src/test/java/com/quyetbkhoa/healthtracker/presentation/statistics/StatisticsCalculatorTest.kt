package com.quyetbkhoa.healthtracker.presentation.statistics

import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.PhysicalActivityRecord
import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange
import com.quyetbkhoa.healthtracker.domain.usecase.EvaluateDailyCalorieGoalUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsCalculatorTest {
    private val instant = Instant.parse("2026-07-24T08:00:00Z")
    private val clock = Clock.fixed(instant, ZoneOffset.UTC)
    private val calculator = StatisticsCalculator(EvaluateDailyCalorieGoalUseCase(), clock)

    @Test
    fun burnedCaloriesIncludeBasalCaloriesAndRecordedActivity() {
        val result = calculator.calculate(
            StatisticsInput(
                range = StatisticsRange.TODAY,
                requestedStartDate = LocalDate.of(2026, 7, 24),
                dailyTarget = 2_000,
                dailyBasalCalories = 1_500,
                goal = Goal.MAINTAIN,
                meals = emptyList(),
                activities = listOf(
                    PhysicalActivityRecord(
                        activityTypeId = 1,
                        durationMinutes = 30,
                        metAtCreation = 3.0,
                        weightKgAtCreation = 70.0,
                        caloriesBurned = 149.6,
                        performedAt = instant.toEpochMilli(),
                        createdAt = instant.toEpochMilli()
                    )
                )
            )
        )

        assertEquals(1_650, result.dailyStatistics.single().burnedCalories)
        assertEquals(1_650, result.burned.total)
        assertEquals(1_650, result.burned.dailyAverage)
    }
}
