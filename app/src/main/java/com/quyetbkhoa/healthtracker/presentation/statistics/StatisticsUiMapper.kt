package com.quyetbkhoa.healthtracker.presentation.statistics

import com.quyetbkhoa.healthtracker.domain.statistics.CalorieStatisticsSummary
import com.quyetbkhoa.healthtracker.domain.statistics.DailyStatistics
import com.quyetbkhoa.healthtracker.domain.statistics.DatedStatistics
import com.quyetbkhoa.healthtracker.domain.statistics.GoalStatisticsSummary
import com.quyetbkhoa.healthtracker.domain.statistics.StatisticsSummary

internal fun StatisticsSummary.toUiState(): StatisticsUiState = StatisticsUiState(
    isLoading = false,
    selectedRange = range,
    dailyTarget = dailyTarget,
    consumed = consumed.toUiModel(),
    burned = burned.toUiModel(),
    goal = goal.toUiModel(),
    dailyStatistics = dailyStatistics.map(DailyStatistics::toUiModel)
)

private fun CalorieStatisticsSummary.toUiModel(): CalorieStatistics = CalorieStatistics(
    total = total,
    dailyAverage = dailyAverage,
    highest = highest.toUiModel()
)

private fun DatedStatistics.toUiModel(): DatedStatistic = DatedStatistic(
    value = value,
    date = date
)

private fun GoalStatisticsSummary.toUiModel(): GoalStatistics = GoalStatistics(
    achievedDays = achievedDays,
    totalDays = totalDays,
    targetDifference = targetDifference,
    achievementRate = achievementRate,
    longestStreak = GoalStreak(
        length = longestStreak.length,
        startDate = longestStreak.startDate,
        endDate = longestStreak.endDate
    ),
    firstAchievedDate = firstAchievedDate,
    lastAchievedDate = lastAchievedDate
)

private fun DailyStatistics.toUiModel(): DailyStatistic = DailyStatistic(
    date = date,
    consumedCalories = consumedCalories,
    burnedCalories = burnedCalories
)
