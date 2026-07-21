package com.quyetbkhoa.healthtracker.domain.model

const val OTHER_ACTIVITY_TYPE_ID = 19L

data class PhysicalActivityType(
    val id: Long,
    val name: String,
    val met: Double,
    val iconName: String,
    val isFavorite: Boolean,
    val displayOrder: Int
)

data class PhysicalActivityRecord(
    val id: Long = 0L,
    val activityTypeId: Long,
    val durationMinutes: Int,
    val metAtCreation: Double,
    val weightKgAtCreation: Double,
    val caloriesBurned: Double,
    val performedAt: Long,
    val createdAt: Long
)
