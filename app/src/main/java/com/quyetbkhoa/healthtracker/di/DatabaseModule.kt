package com.quyetbkhoa.healthtracker.di

import android.content.Context
import androidx.room.Room
import com.quyetbkhoa.healthtracker.data.local.ALL_MIGRATIONS
import com.quyetbkhoa.healthtracker.data.local.DefaultDataCallback
import com.quyetbkhoa.healthtracker.data.local.HealthTrackerDatabase
import com.quyetbkhoa.healthtracker.data.local.activity.ActivityDao
import com.quyetbkhoa.healthtracker.data.local.food.FoodDao
import com.quyetbkhoa.healthtracker.data.local.meal.MealDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): HealthTrackerDatabase = Room.databaseBuilder(
        context,
        HealthTrackerDatabase::class.java,
        "health_tracker.db"
    )
        .addMigrations(*ALL_MIGRATIONS)
        .addCallback(DefaultDataCallback)
        .build()

    @Provides
    fun provideMealDao(database: HealthTrackerDatabase): MealDao = database.mealDao()

    @Provides
    fun provideFoodDao(database: HealthTrackerDatabase): FoodDao = database.foodDao()

    @Provides
    fun provideActivityDao(database: HealthTrackerDatabase): ActivityDao = database.activityDao()
}
