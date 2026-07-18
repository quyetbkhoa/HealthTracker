package com.quyetbkhoa.healthtracker.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class StatisticsRange {
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    ALL
}
