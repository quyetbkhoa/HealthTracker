package com.quyetbkhoa.healthtracker.domain.repository

import com.quyetbkhoa.healthtracker.domain.model.PhysicalActivityRecord
import com.quyetbkhoa.healthtracker.domain.model.PhysicalActivityType
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeActivityTypes(): Flow<List<PhysicalActivityType>>
    fun observeRecordsByDay(epochDay: Long): Flow<List<PhysicalActivityRecord>>
    fun observeTotalCaloriesByDay(epochDay: Long): Flow<Double>
    fun observeTotalCaloriesBetween(startMillis: Long, endMillis: Long): Flow<Double>
    fun observeRecordsBetween(startMillis: Long, endMillis: Long): Flow<List<PhysicalActivityRecord>>
    suspend fun seedDefaultActivities()
    suspend fun getActivityType(id: Long): PhysicalActivityType?
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
    suspend fun addActivityRecord(record: PhysicalActivityRecord)
    suspend fun deleteActivityRecord(id: Long)
    suspend fun clearActivityRecords()
}
