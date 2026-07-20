package com.quyetbkhoa.healthtracker.data.local.food

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey val id: Long,
    val caloriesPer100Grams: Double,
    val defaultServingGrams: Double,
    val isFavorite: Boolean = false,
    val displayOrder: Int
)
