package com.quyetbkhoa.healthtracker.data.repository

import com.quyetbkhoa.healthtracker.data.datastore.DailyCalorieDataStore
import com.quyetbkhoa.healthtracker.domain.model.DailyCalorieSummary
import com.quyetbkhoa.healthtracker.domain.repository.DailyCalorieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DailyCalorieRepositoryImpl @Inject constructor(
    private val dataStore: DailyCalorieDataStore
) : DailyCalorieRepository {
    override fun observeSummary(epochDay: Long): Flow<DailyCalorieSummary> = dataStore.observeSummary(epochDay)
    override suspend fun updateConsumedCalories(epochDay: Long, calories: Int) = dataStore.updateConsumedCalories(epochDay, calories)
    override suspend fun updateExerciseCalories(epochDay: Long, calories: Int) = dataStore.updateExerciseCalories(epochDay, calories)
    override suspend fun clear() = dataStore.clear()
}
