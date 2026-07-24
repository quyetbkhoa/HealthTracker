package com.quyetbkhoa.healthtracker.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quyetbkhoa.healthtracker.data.local.activity.DefaultActivities
import com.quyetbkhoa.healthtracker.data.local.food.DefaultFoods

internal object DefaultDataCallback : RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            seedActivities(db)
            seedFoods(db)
            seedFoodTranslations(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun seedActivities(db: SupportSQLiteDatabase) {
        DefaultActivities.values.forEach { activity ->
            db.execSQL(
                """
                INSERT OR IGNORE INTO activity_types
                (id, name, met, iconName, isFavorite, displayOrder)
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any>(
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

    private fun seedFoods(db: SupportSQLiteDatabase) {
        DefaultFoods.foods.forEach { food ->
            db.execSQL(
                """
                INSERT OR IGNORE INTO foods
                (id, caloriesPer100Grams, defaultServingGrams, isFavorite, displayOrder)
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any>(
                    food.id,
                    food.caloriesPer100Grams,
                    food.defaultServingGrams,
                    if (food.isFavorite) 1 else 0,
                    food.displayOrder
                )
            )
        }
    }

    private fun seedFoodTranslations(db: SupportSQLiteDatabase) {
        DefaultFoods.translations.forEach { translation ->
            db.execSQL(
                """
                INSERT OR IGNORE INTO food_translations
                (foodId, languageTag, name, normalizedName)
                VALUES (?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any>(
                    translation.foodId,
                    translation.languageTag,
                    translation.name,
                    translation.normalizedName
                )
            )
        }
    }
}
