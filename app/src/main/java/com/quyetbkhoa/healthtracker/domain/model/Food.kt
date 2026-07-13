package com.quyetbkhoa.healthtracker.domain.model

data class Food(
    val id: Long,
    val name: String,
    val caloriesPer100Grams: Double,
    val defaultServingGrams: Double,
    val displayOrder: Int
)
