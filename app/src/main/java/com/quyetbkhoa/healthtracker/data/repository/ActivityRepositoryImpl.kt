package com.quyetbkhoa.healthtracker.data.repository

import com.quyetbkhoa.healthtracker.data.local.activity.ActivityDao
import com.quyetbkhoa.healthtracker.data.local.activity.ActivityRecordEntity
import com.quyetbkhoa.healthtracker.data.local.activity.ActivityTypeEntity
import com.quyetbkhoa.healthtracker.domain.model.PhysicalActivityRecord
import com.quyetbkhoa.healthtracker.domain.model.PhysicalActivityType
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao
) : ActivityRepository {
    override fun observeActivityTypes(): Flow<List<PhysicalActivityType>> =
        activityDao.observeActivityTypes().map { items -> items.map(ActivityTypeEntity::toDomain) }

    override fun observeRecordsByDay(epochDay: Long): Flow<List<PhysicalActivityRecord>> {
        val (start, end) = dayRange(epochDay)
        return observeRecordsBetween(start, end)
    }

    override fun observeTotalCaloriesByDay(epochDay: Long): Flow<Double> {
        val (start, end) = dayRange(epochDay)
        return activityDao.observeTotalCaloriesBetween(start, end)
    }

    override fun observeTotalCaloriesBetween(startMillis: Long, endMillis: Long): Flow<Double> =
        activityDao.observeTotalCaloriesBetween(startMillis, endMillis)

    override fun observeRecordsBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<PhysicalActivityRecord>> =
        activityDao.observeRecordsBetween(startMillis, endMillis)
            .map { records -> records.map(ActivityRecordEntity::toDomain) }

    override suspend fun seedDefaultActivities() = activityDao.seedDefaults()

    override suspend fun getActivityType(id: Long): PhysicalActivityType? =
        activityDao.getActivityType(id)?.toDomain()

    override suspend fun setFavorite(id: Long, isFavorite: Boolean) =
        activityDao.updateFavorite(id, isFavorite)

    override suspend fun addActivityRecord(record: PhysicalActivityRecord) {
        activityDao.insertActivityRecord(record.toEntity())
    }

    override suspend fun deleteActivityRecord(id: Long) = activityDao.deleteActivityRecord(id)

    override suspend fun clearActivityRecords() = activityDao.deleteAllActivityRecords()

    private fun dayRange(epochDay: Long): Pair<Long, Long> {
        val date = LocalDate.ofEpochDay(epochDay)
        val zone = ZoneId.systemDefault()
        return date.atStartOfDay(zone).toInstant().toEpochMilli() to
            date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    }
}

private fun ActivityTypeEntity.toDomain() = PhysicalActivityType(
    id = id,
    name = name,
    met = met,
    iconName = iconName,
    isFavorite = isFavorite,
    displayOrder = displayOrder
)

private fun ActivityRecordEntity.toDomain() = PhysicalActivityRecord(
    id = id,
    activityTypeId = activityTypeId,
    durationMinutes = durationMinutes,
    metAtCreation = metAtCreation,
    weightKgAtCreation = weightKgAtCreation,
    caloriesBurned = caloriesBurned,
    performedAt = performedAt,
    createdAt = createdAt
)

private fun PhysicalActivityRecord.toEntity() = ActivityRecordEntity(
    id = id,
    activityTypeId = activityTypeId,
    durationMinutes = durationMinutes,
    metAtCreation = metAtCreation,
    weightKgAtCreation = weightKgAtCreation,
    caloriesBurned = caloriesBurned,
    performedAt = performedAt,
    createdAt = createdAt
)
