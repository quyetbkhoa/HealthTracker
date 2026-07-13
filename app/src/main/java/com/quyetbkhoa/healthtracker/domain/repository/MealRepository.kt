package com.quyetbkhoa.healthtracker.domain.repository

import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun observeMealsByDay(epochDay: Long, languageTag: String): Flow<List<MealEntry>>
    suspend fun addMeal(meal: MealEntry)
    suspend fun updateMeal(meal: MealEntry)
    suspend fun deleteMeal(id: Long)
    suspend fun clearMeals()
}
