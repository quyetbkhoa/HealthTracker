package com.quyetbkhoa.healthtracker.data.local.food

import androidx.room.Entity
import androidx.room.Index


@Entity(
    tableName = "foods",
    indices = [Index(value = ["normalizedName"])]
)
data class FoodEntity(
    val id: Long,
    val name: String,
    val normalizedName: String,
    val servingAmount: Double,
    val servingUnit: String,
    val caloriesPerServing: Double,
    val displayOrder: Int
)