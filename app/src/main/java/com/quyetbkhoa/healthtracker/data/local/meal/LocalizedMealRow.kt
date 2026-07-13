package com.quyetbkhoa.healthtracker.data.local.meal

data class LocalizedMealRow(
    val id: Long,
    val foodId: Long?,
    val displayName: String,
    val nameSnapshot: String,
    val calories: Int,
    val mealType: String,
    val consumedGrams: Double,
    val caloriesPer100GramsSnapshot: Double,
    val eatenAt: Long
)
