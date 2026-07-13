package com.quyetbkhoa.healthtracker.data.local.meal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert
    suspend fun insert(meal: MealEntity): Long

    @Update
    suspend fun update(meal: MealEntity)

    @Delete
    suspend fun delete(meal: MealEntity)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM meals")
    suspend fun deleteAll()

    @Query(
        """
        SELECT
            meals.id AS id,
            meals.foodId AS foodId,
            COALESCE(food_translations.name, meals.nameSnapshot) AS displayName,
            meals.nameSnapshot AS nameSnapshot,
            meals.calories AS calories,
            meals.mealType AS mealType,
            meals.consumedGrams AS consumedGrams,
            meals.caloriesPer100GramsSnapshot AS caloriesPer100GramsSnapshot,
            meals.eatenAt AS eatenAt
        FROM meals
        LEFT JOIN food_translations
            ON meals.foodId = food_translations.foodId
            AND food_translations.languageTag = :languageTag
        WHERE eatenAt >= :startMillis AND eatenAt < :endMillis
        ORDER BY eatenAt ASC
        """
    )
    fun observeMealsBetween(
        startMillis: Long,
        endMillis: Long,
        languageTag: String
    ): Flow<List<LocalizedMealRow>>
}
