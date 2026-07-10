package com.quyetbkhoa.healthtracker.domain.model

data class DailyCalorieSummary(
    val epochDay: Long,
    val consumedCalories: Int = 0,
    val exerciseCalories: Int = 0
)
