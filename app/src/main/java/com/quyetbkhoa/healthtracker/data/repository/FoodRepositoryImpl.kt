package com.quyetbkhoa.healthtracker.data.repository

import com.quyetbkhoa.healthtracker.data.local.food.FoodDao
import com.quyetbkhoa.healthtracker.data.local.food.FoodNameNormalizer
import com.quyetbkhoa.healthtracker.data.local.food.LocalizedFoodRow
import com.quyetbkhoa.healthtracker.domain.model.Food
import com.quyetbkhoa.healthtracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao
) : FoodRepository {
    override fun observeFoods(query: String, languageTag: String): Flow<List<Food>> =
        foodDao.observeLocalizedFoods(
            languageTag = languageTag.safeLanguageTag(),
            normalizedQuery = FoodNameNormalizer.normalize(query)
        ).map { rows -> rows.map(LocalizedFoodRow::toDomain) }

    override suspend fun getFoodById(id: Long, languageTag: String): Food? {
        val requestedTag = languageTag.safeLanguageTag()
        val row = foodDao.getLocalizedFoodById(id, requestedTag)
            ?: foodDao.getLocalizedFoodById(id, DEFAULT_LANGUAGE_TAG)
            ?: foodDao.getLocalizedFoodById(id, ENGLISH_LANGUAGE_TAG)
        return row?.toDomain()
    }

    private fun String.safeLanguageTag(): String =
        if (startsWith(ENGLISH_LANGUAGE_TAG)) ENGLISH_LANGUAGE_TAG else DEFAULT_LANGUAGE_TAG

    private companion object {
        const val DEFAULT_LANGUAGE_TAG = "vi"
        const val ENGLISH_LANGUAGE_TAG = "en"
    }
}

private fun LocalizedFoodRow.toDomain() = Food(
    id = id,
    name = name,
    caloriesPer100Grams = caloriesPer100Grams,
    defaultServingGrams = defaultServingGrams,
    displayOrder = displayOrder
)
