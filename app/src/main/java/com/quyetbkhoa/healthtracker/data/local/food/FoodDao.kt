package com.quyetbkhoa.healthtracker.data.local.food

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFoods(foods: List<FoodEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTranslations(
        translations: List<FoodTranslationEntity>
    ): List<Long>

    @Query(
        """
        SELECT
            foods.id AS id,
            food_translations.name AS name,
            foods.caloriesPer100Grams AS caloriesPer100Grams,
            foods.defaultServingGrams AS defaultServingGrams,
            foods.isFavorite AS isFavorite,
            foods.displayOrder AS displayOrder
        FROM foods
        INNER JOIN food_translations
            ON foods.id = food_translations.foodId
        WHERE food_translations.languageTag = :languageTag
          AND (
              :normalizedQuery = ''
              OR food_translations.normalizedName
                  LIKE '%' || :normalizedQuery || '%'
          )
        ORDER BY foods.isFavorite DESC, foods.displayOrder ASC, food_translations.name ASC
        """
    )
    fun observeLocalizedFoods(
        languageTag: String,
        normalizedQuery: String
    ): Flow<List<LocalizedFoodRow>>

    @Query(
        """
        SELECT
            foods.id AS id,
            food_translations.name AS name,
            foods.caloriesPer100Grams AS caloriesPer100Grams,
            foods.defaultServingGrams AS defaultServingGrams,
            foods.isFavorite AS isFavorite,
            foods.displayOrder AS displayOrder
        FROM foods
        INNER JOIN food_translations
            ON foods.id = food_translations.foodId
        WHERE foods.id = :foodId
          AND food_translations.languageTag = :languageTag
        LIMIT 1
        """
    )
    suspend fun getLocalizedFoodById(
        foodId: Long,
        languageTag: String
    ): LocalizedFoodRow?

    @Query("UPDATE foods SET isFavorite = :isFavorite WHERE id = :foodId")
    suspend fun updateFavorite(foodId: Long, isFavorite: Boolean)
}
