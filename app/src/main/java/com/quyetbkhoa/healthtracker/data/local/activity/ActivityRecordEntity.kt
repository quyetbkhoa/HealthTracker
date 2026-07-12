package com.quyetbkhoa.healthtracker.data.local.activity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_records",
    foreignKeys = [
        ForeignKey(
            entity = ActivityTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityTypeId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("activityTypeId"), Index("performedAt")]
)
data class ActivityRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val activityTypeId: Long,
    val durationMinutes: Int,
    val metAtCreation: Double,
    val weightKgAtCreation: Double,
    val caloriesBurned: Double,
    val performedAt: Long,
    val createdAt: Long
)
