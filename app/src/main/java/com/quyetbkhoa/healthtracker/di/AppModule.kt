package com.quyetbkhoa.healthtracker.di

import android.content.Context
import androidx.room.Room
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.quyetbkhoa.healthtracker.data.datastore.SettingsDataStore
import com.quyetbkhoa.healthtracker.data.repository.SettingsRepositoryImpl
import com.quyetbkhoa.healthtracker.domain.repository.SettingsRepository
import com.quyetbkhoa.healthtracker.data.datastore.ProfileDataStore
import com.quyetbkhoa.healthtracker.data.repository.ProfileRepositoryImpl
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.data.datastore.DailyCalorieDataStore
import com.quyetbkhoa.healthtracker.data.repository.DailyCalorieRepositoryImpl
import com.quyetbkhoa.healthtracker.domain.repository.DailyCalorieRepository
import com.quyetbkhoa.healthtracker.data.local.HealthTrackerDatabase
import com.quyetbkhoa.healthtracker.data.local.meal.MealDao
import com.quyetbkhoa.healthtracker.data.repository.MealRepositoryImpl
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHealthTrackerDatabase(
        @ApplicationContext context: Context
    ): HealthTrackerDatabase =
        Room.databaseBuilder(
            context,
            HealthTrackerDatabase::class.java,
            "health_tracker.db"
        ).build()

    @Provides
    fun provideMealDao(database: HealthTrackerDatabase): MealDao = database.mealDao()

    @Provides
    @Singleton
    fun provideMealRepository(mealDao: MealDao): MealRepository = MealRepositoryImpl(mealDao)

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings") }
        )
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(dataStore: DataStore<Preferences>): SettingsDataStore {
        return SettingsDataStore(dataStore)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(settingsDataStore: SettingsDataStore): SettingsRepository {
        return SettingsRepositoryImpl(settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideProfileDataStore(dataStore: DataStore<Preferences>): ProfileDataStore {
        return ProfileDataStore(dataStore)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(profileDataStore: ProfileDataStore): ProfileRepository {
        return ProfileRepositoryImpl(profileDataStore)
    }

    @Provides
    @Singleton
    fun provideDailyCalorieDataStore(dataStore: DataStore<Preferences>): DailyCalorieDataStore =
        DailyCalorieDataStore(dataStore)

    @Provides
    @Singleton
    fun provideDailyCalorieRepository(dataStore: DailyCalorieDataStore): DailyCalorieRepository =
        DailyCalorieRepositoryImpl(dataStore)
}
