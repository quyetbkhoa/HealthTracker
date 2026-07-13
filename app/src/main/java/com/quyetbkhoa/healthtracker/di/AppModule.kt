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
import com.quyetbkhoa.healthtracker.data.local.food.DefaultFoods
import com.quyetbkhoa.healthtracker.data.local.food.FoodDao
import com.quyetbkhoa.healthtracker.data.local.meal.MealDao
import com.quyetbkhoa.healthtracker.data.repository.MealRepositoryImpl
import com.quyetbkhoa.healthtracker.data.repository.FoodRepositoryImpl
import com.quyetbkhoa.healthtracker.domain.repository.FoodRepository
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import com.quyetbkhoa.healthtracker.data.repository.ActivityRepositoryImpl
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .addCallback(DEFAULT_DATA_SEED_CALLBACK)
            .build()

    @Provides
    fun provideMealDao(database: HealthTrackerDatabase): MealDao = database.mealDao()

    @Provides
    fun provideFoodDao(database: HealthTrackerDatabase): FoodDao = database.foodDao()

    @Provides
    @Singleton
    fun provideMealRepository(mealDao: MealDao): MealRepository = MealRepositoryImpl(mealDao)

    @Provides
    @Singleton
    fun provideFoodRepository(foodDao: FoodDao): FoodRepository = FoodRepositoryImpl(foodDao)

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

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS foods (
                    id INTEGER NOT NULL,
                    caloriesPer100Grams REAL NOT NULL,
                    defaultServingGrams REAL NOT NULL,
                    displayOrder INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS food_translations (
                    foodId INTEGER NOT NULL,
                    languageTag TEXT NOT NULL,
                    name TEXT NOT NULL,
                    normalizedName TEXT NOT NULL,
                    PRIMARY KEY(foodId, languageTag),
                    FOREIGN KEY(foodId) REFERENCES foods(id)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_food_translations_foodId " +
                    "ON food_translations(foodId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_food_translations_languageTag_normalizedName " +
                    "ON food_translations(languageTag, normalizedName)"
            )
            db.execSQL(
                """
                CREATE TABLE meals_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    foodId INTEGER,
                    nameSnapshot TEXT NOT NULL,
                    calories INTEGER NOT NULL,
                    mealType TEXT NOT NULL,
                    consumedGrams REAL NOT NULL,
                    caloriesPer100GramsSnapshot REAL NOT NULL,
                    eatenAt INTEGER NOT NULL,
                    FOREIGN KEY(foodId) REFERENCES foods(id)
                        ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO meals_new (
                    id, foodId, nameSnapshot, calories, mealType,
                    consumedGrams, caloriesPer100GramsSnapshot, eatenAt
                )
                SELECT id, NULL, name, calories, mealType,
                    100.0, CAST(calories AS REAL), eatenAt
                FROM meals
                """.trimIndent()
            )
            db.execSQL("DROP TABLE meals")
            db.execSQL("ALTER TABLE meals_new RENAME TO meals")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_meals_foodId ON meals(foodId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_meals_eatenAt ON meals(eatenAt)")
        }
    }

    private val DEFAULT_DATA_SEED_CALLBACK = object : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            db.beginTransaction()
            try {
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
                DefaultFoods.foods.forEach { food ->
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO foods
                        (id, caloriesPer100Grams, defaultServingGrams, displayOrder)
                        VALUES (?, ?, ?, ?)
                        """.trimIndent(),
                        arrayOf(
                            food.id,
                            food.caloriesPer100Grams,
                            food.defaultServingGrams,
                            food.displayOrder
                        )
                    )
                }
                DefaultFoods.translations.forEach { translation ->
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO food_translations
                        (foodId, languageTag, name, normalizedName)
                        VALUES (?, ?, ?, ?)
                        """.trimIndent(),
                        arrayOf(
                            translation.foodId,
                            translation.languageTag,
                            translation.name,
                            translation.normalizedName
                        )
                    )
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
