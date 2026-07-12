package com.quyetbkhoa.healthtracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.quyetbkhoa.healthtracker.data.local.activity.ActivityDao
import com.quyetbkhoa.healthtracker.data.local.activity.DefaultActivities
import com.quyetbkhoa.healthtracker.data.local.meal.MealDao
import com.quyetbkhoa.healthtracker.data.repository.MealRepositoryImpl
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import com.quyetbkhoa.healthtracker.data.repository.ActivityRepositoryImpl
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
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
        )
            .addMigrations(MIGRATION_1_2)
            .addCallback(ACTIVITY_SEED_CALLBACK)
            .build()

    @Provides
    fun provideMealDao(database: HealthTrackerDatabase): MealDao = database.mealDao()

    @Provides
    @Singleton
    fun provideMealRepository(mealDao: MealDao): MealRepository = MealRepositoryImpl(mealDao)

    @Provides
    fun provideActivityDao(database: HealthTrackerDatabase): ActivityDao = database.activityDao()

    @Provides
    @Singleton
    fun provideActivityRepository(activityDao: ActivityDao): ActivityRepository =
        ActivityRepositoryImpl(activityDao)

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

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS activity_types (
                    id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    met REAL NOT NULL,
                    iconName TEXT NOT NULL,
                    isFavorite INTEGER NOT NULL,
                    displayOrder INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS activity_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    activityTypeId INTEGER NOT NULL,
                    durationMinutes INTEGER NOT NULL,
                    metAtCreation REAL NOT NULL,
                    weightKgAtCreation REAL NOT NULL,
                    caloriesBurned REAL NOT NULL,
                    performedAt INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(activityTypeId) REFERENCES activity_types(id)
                        ON UPDATE NO ACTION ON DELETE RESTRICT
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_activity_records_activityTypeId ON activity_records(activityTypeId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_activity_records_performedAt ON activity_records(performedAt)")
        }
    }

    private val ACTIVITY_SEED_CALLBACK = object : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            DefaultActivities.values.forEach { activity ->
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO activity_types
                    (id, name, met, iconName, isFavorite, displayOrder)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    arrayOf(
                        activity.id,
                        activity.name,
                        activity.met,
                        activity.iconName,
                        if (activity.isFavorite) 1 else 0,
                        activity.displayOrder
                    )
                )
            }
        }
    }
}
