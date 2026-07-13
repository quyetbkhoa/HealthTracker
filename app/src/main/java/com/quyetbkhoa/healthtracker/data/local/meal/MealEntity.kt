package com.quyetbkhoa.healthtracker.data.local.meal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.quyetbkhoa.healthtracker.data.local.food.FoodEntity

@Entity(
    tableName = "meals",
    foreignKeys = [
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("foodId"), Index("eatenAt")]
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val foodId: Long?,
    val nameSnapshot: String,
    val calories: Int,
    val mealType: String,
    val consumedGrams: Double,
    val caloriesPer100GramsSnapshot: Double,
    val eatenAt: Long
)
