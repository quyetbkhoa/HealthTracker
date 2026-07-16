package com.quyetbkhoa.healthtracker.presentation.statistics

import androidx.compose.runtime.Immutable
import java.time.LocalDate

enum class StatisticsRange {
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    ALL
}

sealed interface StatisticsAction {
    data class SelectRange(val range: StatisticsRange) : StatisticsAction
}

@Immutable
data class DailyStatistic(
    val date: LocalDate,
    val consumedCalories: Int = 0,
    val burnedCalories: Int = 0
)

@Immutable
data class DatedStatistic(
    val value: Int = 0,
    val date: LocalDate? = null
)

@Immutable
data class CalorieStatistics(
    val total: Int = 0,
    val dailyAverage: Int = 0,
    val highest: DatedStatistic = DatedStatistic()
)

@Immutable
data class GoalStreak(
    val length: Int = 0,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

@Immutable
data class GoalStatistics(
    val achievedDays: Int = 0,
    val totalDays: Int = 0,
    val targetDifference: Int = 0,
    val achievementRate: Int = 0,
    val longestStreak: GoalStreak = GoalStreak(),
    val firstAchievedDate: LocalDate? = null,
    val lastAchievedDate: LocalDate? = null
)

@Immutable
data class StatisticsUiState(
    val isLoading: Boolean = true,
    val selectedRange: StatisticsRange = StatisticsRange.LAST_7_DAYS,
    val dailyTarget: Int = 0,
    val consumed: CalorieStatistics = CalorieStatistics(),
    val burned: CalorieStatistics = CalorieStatistics(),
    val goal: GoalStatistics = GoalStatistics(),
    val dailyStatistics: List<DailyStatistic> = emptyList()
)
