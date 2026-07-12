package com.quyetbkhoa.healthtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quyetbkhoa.healthtracker.data.local.activity.ActivityDao
import com.quyetbkhoa.healthtracker.data.local.activity.ActivityRecordEntity
import com.quyetbkhoa.healthtracker.data.local.activity.ActivityTypeEntity
import com.quyetbkhoa.healthtracker.data.local.meal.MealDao
import com.quyetbkhoa.healthtracker.data.local.meal.MealEntity

@Database(
    entities = [MealEntity::class, ActivityTypeEntity::class, ActivityRecordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class HealthTrackerDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun activityDao(): ActivityDao
}
