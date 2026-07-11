package com.quyetbkhoa.healthtracker.data.local.meal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val calories: Int,
    val mealType: String,
    val eatenAt: Long
)
