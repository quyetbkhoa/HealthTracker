package com.quyetbkhoa.healthtracker.data.local.food

data class LocalizedFoodRow(
    val id: Long,
    val name: String,
    val caloriesPer100Grams: Double,
    val defaultServingGrams: Double,
    val displayOrder: Int
)