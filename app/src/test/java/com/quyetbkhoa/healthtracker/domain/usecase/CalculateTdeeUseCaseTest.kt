package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import com.quyetbkhoa.healthtracker.domain.model.Gender
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class CalculateTdeeUseCaseTest {
    private val calculateTdee = CalculateTdeeUseCase()
    private val today = LocalDate.of(2026, 7, 14)

    @Test
    fun maleMaintain_usesMaleFormulaAndActivityFactor() {
        val result = calculateTdee(profile(Gender.MALE, Goal.MAINTAIN), today)
        assertEquals(1649, result.bmrCalories)
        assertEquals(2267, result.tdeeCalories)
        assertEquals(2267, result.targetCalories)
    }

    @Test
    fun goals_useSafePercentageBasedAdjustments() {
        val maintain = calculateTdee(profile(Gender.FEMALE, Goal.MAINTAIN), today)
        val lose = calculateTdee(profile(Gender.FEMALE, Goal.LOSE_WEIGHT), today)
        val gain = calculateTdee(profile(Gender.FEMALE, Goal.GAIN_WEIGHT), today)
        assertEquals(maintain.targetCalories - 306, lose.targetCalories)
        assertEquals(maintain.targetCalories + 204, gain.targetCalories)
    }

    private fun profile(gender: Gender, goal: Goal) = UserProfile(
        fullName = "Test",
        dateOfBirth = LocalDate.of(1996, 7, 14).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        gender = gender,
        weightKg = 70f,
        heightCm = 175f,
        activityLevel = ActivityLevel.LIGHT,
        goal = goal
    )
}
