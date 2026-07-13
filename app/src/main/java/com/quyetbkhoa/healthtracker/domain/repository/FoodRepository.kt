package com.quyetbkhoa.healthtracker.domain.repository

import com.quyetbkhoa.healthtracker.domain.model.Food
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun observeFoods(query: String, languageTag: String): Flow<List<Food>>
    suspend fun getFoodById(id: Long, languageTag: String): Food?
}
