package com.quyetbkhoa.healthtracker.data.local.activity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT COUNT(*) FROM activity_records")
    suspend fun countActivityRecords(): Int

    @Query("SELECT * FROM activity_types ORDER BY isFavorite DESC, displayOrder ASC, name ASC")
    fun observeActivityTypes(): Flow<List<ActivityTypeEntity>>

    @Query("SELECT * FROM activity_types WHERE id = :id")
    suspend fun getActivityType(id: Long): ActivityTypeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertActivityTypes(items: List<ActivityTypeEntity>): List<Long>

    @Query("UPDATE activity_types SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Insert
    suspend fun insertActivityRecord(record: ActivityRecordEntity): Long

    @Insert
    suspend fun insertActivityRecords(records: List<ActivityRecordEntity>): List<Long>

    @Query(
        """
        SELECT * FROM activity_records
        WHERE performedAt >= :startMillis AND performedAt < :endMillis
        ORDER BY performedAt DESC
        """
    )
    fun observeRecordsBetween(startMillis: Long, endMillis: Long): Flow<List<ActivityRecordEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(caloriesBurned), 0.0) FROM activity_records
        WHERE performedAt >= :startMillis AND performedAt < :endMillis
        """
    )
    fun observeTotalCaloriesBetween(startMillis: Long, endMillis: Long): Flow<Double>

    @Query("DELETE FROM activity_records WHERE id = :id")
    suspend fun deleteActivityRecord(id: Long)

    @Query("DELETE FROM activity_records")
    suspend fun deleteAllActivityRecords()

    @Transaction
    suspend fun seedDefaults() {
        insertActivityTypes(DefaultActivities.values)
    }
}
