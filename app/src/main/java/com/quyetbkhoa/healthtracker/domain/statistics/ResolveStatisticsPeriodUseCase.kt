package com.quyetbkhoa.healthtracker.domain.statistics

import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class StatisticsPeriod(
    val startDate: LocalDate,
    val startMillis: Long,
    val endMillis: Long
)

class ResolveStatisticsPeriodUseCase @Inject constructor(
    private val clock: Clock
) {
    operator fun invoke(range: StatisticsRange): StatisticsPeriod {
        val today = LocalDate.now(clock)
        val startDate = when (range) {
            StatisticsRange.TODAY -> today
            StatisticsRange.LAST_7_DAYS -> today.minusDays(6)
            StatisticsRange.LAST_30_DAYS -> today.minusDays(29)
            StatisticsRange.ALL -> LocalDate.ofEpochDay(0)
        }
        return StatisticsPeriod(
            startDate = startDate,
            startMillis = startDate.atStartOfDay(clock.zone).toInstant().toEpochMilli(),
            endMillis = today.plusDays(1).atStartOfDay(clock.zone).toInstant().toEpochMilli()
        )
    }
}
