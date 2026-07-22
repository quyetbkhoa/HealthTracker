package com.quyetbkhoa.healthtracker.domain.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class StatisticsRange {
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    ALL
}
