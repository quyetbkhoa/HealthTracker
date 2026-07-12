package com.quyetbkhoa.healthtracker.data.local.activity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_types")
data class ActivityTypeEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val met: Double,
    val iconName: String,
    val isFavorite: Boolean = false,
    val displayOrder: Int = 0
)
