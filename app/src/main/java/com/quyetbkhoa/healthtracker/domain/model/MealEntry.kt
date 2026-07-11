package com.quyetbkhoa.healthtracker.domain.model


enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}
data class MealEntry(
    val id: Long = 0L,
    val name: String,
    val calories: Int,
    val mealType: MealType,
    val eatenAt: Long
)
