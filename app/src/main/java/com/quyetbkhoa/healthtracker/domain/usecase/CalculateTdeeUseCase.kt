package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import com.quyetbkhoa.healthtracker.domain.model.Gender
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.roundToInt

data class TdeeResult(
    val age: Int,
    val bmrCalories: Int,
    val tdeeCalories: Int,
    val targetCalories: Int
)

class CalculateTdeeUseCase @Inject constructor() {
    operator fun invoke(profile: UserProfile, today: LocalDate = LocalDate.now()): TdeeResult {
        val birthDate = Instant.ofEpochMilli(profile.dateOfBirth)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val age = ChronoUnit.YEARS.between(birthDate, today).toInt().coerceAtLeast(0)
        val genderAdjustment = if (profile.gender == Gender.FEMALE) -161.0 else 5.0
        val bmr = 10.0 * profile.weightKg + 6.25 * profile.heightCm - 5.0 * age + genderAdjustment
        val activityFactor = when (profile.activityLevel) {
            ActivityLevel.SEDENTARY -> 1.2
            ActivityLevel.LIGHT -> 1.375
            ActivityLevel.MODERATE -> 1.55
            ActivityLevel.VERY_ACTIVE -> 1.725
            ActivityLevel.EXTRA_ACTIVE -> 1.9
        }
        val tdee = (bmr * activityFactor).roundToInt()
        val goalAdjustment = when (profile.goal) {
            Goal.LOSE_WEIGHT -> -(tdee * 0.15).coerceIn(250.0, 500.0).roundToInt()
            Goal.MAINTAIN -> 0
            Goal.GAIN_WEIGHT -> (tdee * 0.10).coerceIn(200.0, 350.0).roundToInt()
        }
        return TdeeResult(
            age = age,
            bmrCalories = bmr.roundToInt(),
            tdeeCalories = tdee,
            targetCalories = (tdee + goalAdjustment).coerceAtLeast(1_200)
        )
    }
}
