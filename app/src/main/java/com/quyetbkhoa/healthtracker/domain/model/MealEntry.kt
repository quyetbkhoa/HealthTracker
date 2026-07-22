package com.quyetbkhoa.healthtracker.domain.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}
data class MealEntry(
    val id: Long = 0L,
    val foodId: Long? = null,
    val name: String,
    val nameSnapshot: String = name,
    val calories: Int,
    val mealType: MealType,
    val consumedGrams: Double,
    val caloriesPer100GramsSnapshot: Double,
    val eatenAt: Long
)
