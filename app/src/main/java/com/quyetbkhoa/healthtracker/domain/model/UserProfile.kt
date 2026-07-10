package com.quyetbkhoa.healthtracker.domain.model

data class UserProfile(
    val fullName: String = "",
    val dateOfBirth: Long = 0L, // Epoch milliseconds
    val gender: Gender = Gender.OTHER,
    val weightKg: Float = 0f,
    val heightCm: Float = 0f,
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    val goal: Goal = Goal.MAINTAIN,
    val bmrCalories: Int = 0,
    val tdeeCalories: Int = 0,
    val dailyCalorieTarget: Int = 0
)

enum class Gender {
    MALE, FEMALE, OTHER
}

enum class ActivityLevel {
    SEDENTARY,
    LIGHT,
    MODERATE,
    VERY_ACTIVE,
    EXTRA_ACTIVE
}

enum class Goal {
    LOSE_WEIGHT,
    MAINTAIN,
    GAIN_WEIGHT
}
