package com.quyetbkhoa.healthtracker.domain.statistics

import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.PhysicalActivityRecord
import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange
import java.time.LocalDate

data class StatisticsCalculationInput(
    val range: StatisticsRange,
    val requestedStartDate: LocalDate,
    val dailyTarget: Int,
    val dailyBasalCalories: Int,
    val goal: Goal,
    val meals: List<MealEntry>,
    val activities: List<PhysicalActivityRecord>
)

data class DailyStatistics(
    val date: LocalDate,
    val consumedCalories: Int = 0,
    val burnedCalories: Int = 0
)

data class DatedStatistics(
    val value: Int = 0,
    val date: LocalDate? = null
)

data class CalorieStatisticsSummary(
    val total: Int = 0,
    val dailyAverage: Int = 0,
    val highest: DatedStatistics = DatedStatistics()
)

data class GoalStreak(
    val length: Int = 0,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

data class GoalStatisticsSummary(
    val achievedDays: Int = 0,
    val totalDays: Int = 0,
    val targetDifference: Int = 0,
    val achievementRate: Int = 0,
    val longestStreak: GoalStreak = GoalStreak(),
    val firstAchievedDate: LocalDate? = null,
    val lastAchievedDate: LocalDate? = null
)

data class StatisticsSummary(
    val range: StatisticsRange,
    val dailyTarget: Int,
    val consumed: CalorieStatisticsSummary,
    val burned: CalorieStatisticsSummary,
    val goal: GoalStatisticsSummary,
    val dailyStatistics: List<DailyStatistics>
)
