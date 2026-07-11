package com.quyetbkhoa.healthtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quyetbkhoa.healthtracker.data.local.meal.MealDao
import com.quyetbkhoa.healthtracker.data.local.meal.MealEntity

@Database(
    entities = [MealEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HealthTrackerDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
}
