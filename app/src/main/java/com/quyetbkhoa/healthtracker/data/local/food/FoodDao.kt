package com.quyetbkhoa.healthtracker.data.local.food

import androidx.room.Dao
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    fun searchFoods(query: String): Flow<List<FoodEntity>>
    fun observeFoods(): Flow<List<FoodEntity>>
    suspend fun getById(id: Long): FoodEntity?
    suspend fun insertIgnore(foods: List<FoodEntity>)
}