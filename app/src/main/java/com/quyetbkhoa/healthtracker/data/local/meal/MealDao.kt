package com.quyetbkhoa.healthtracker.data.local.meal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert
    suspend fun insert(meal: MealEntity): Long

    @Update
    suspend fun update(meal: MealEntity)

    @Delete
    suspend fun delete(meal: MealEntity)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM meals")
    suspend fun deleteAll()

    @Query(
        """
        SELECT * FROM meals
        WHERE eatenAt >= :startMillis AND eatenAt < :endMillis
        ORDER BY eatenAt ASC
        """
    )
    fun observeMealsBetween(startMillis: Long, endMillis: Long): Flow<List<MealEntity>>
}
