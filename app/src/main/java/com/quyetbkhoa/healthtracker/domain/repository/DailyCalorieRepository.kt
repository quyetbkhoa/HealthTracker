package com.quyetbkhoa.healthtracker.domain.repository

import com.quyetbkhoa.healthtracker.domain.model.DailyCalorieSummary
import kotlinx.coroutines.flow.Flow

interface DailyCalorieRepository {
    fun observeSummary(epochDay: Long): Flow<DailyCalorieSummary>
    suspend fun updateConsumedCalories(epochDay: Long, calories: Int)
    suspend fun updateExerciseCalories(epochDay: Long, calories: Int)
    suspend fun clear()
}
