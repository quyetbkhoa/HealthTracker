package com.quyetbkhoa.healthtracker.domain.statistics

import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieStatus
import com.quyetbkhoa.healthtracker.domain.usecase.EvaluateDailyCalorieGoalUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

class CalculateStatisticsUseCase @Inject constructor(
    private val evaluateDailyGoal: EvaluateDailyCalorieGoalUseCase,
    private val clock: Clock
) {
    operator fun invoke(input: StatisticsCalculationInput): StatisticsSummary {
        val dailyStatistics = buildDailyStatistics(input, LocalDate.now(clock))
        val achievedDates = dailyStatistics
            .filter { it.isGoalAchieved(input.dailyTarget, input.goal) }
            .map(DailyStatistics::date)

        return StatisticsSummary(
            range = input.range,
            dailyTarget = input.dailyTarget,
            consumed = dailyStatistics.toCalorieSummary(DailyStatistics::consumedCalories),
            burned = dailyStatistics.toCalorieSummary(DailyStatistics::burnedCalories),
            goal = buildGoalStatistics(dailyStatistics, achievedDates, input.dailyTarget),
            dailyStatistics = dailyStatistics
        )
    }

    private fun buildDailyStatistics(
        input: StatisticsCalculationInput,
        today: LocalDate
    ): List<DailyStatistics> {
        val mealsByDate = input.meals.groupBy { it.eatenAt.toLocalDate() }
        val activitiesByDate = input.activities.groupBy { it.performedAt.toLocalDate() }
        val firstRecordedDate = (mealsByDate.keys + activitiesByDate.keys).minOrNull()
        val startDate = if (input.range == StatisticsRange.ALL) {
            firstRecordedDate ?: today
        } else {
            input.requestedStartDate
        }

        return datesBetween(startDate, today).map { date ->
            DailyStatistics(
                date = date,
                consumedCalories = mealsByDate[date].orEmpty().sumOf(MealEntry::calories),
                burnedCalories = input.dailyBasalCalories.coerceAtLeast(0) +
                    activitiesByDate[date].orEmpty()
                        .sumOf { it.caloriesBurned }
                        .roundToInt()
            )
        }
    }

    private fun buildGoalStatistics(
        dailyStatistics: List<DailyStatistics>,
        achievedDates: List<LocalDate>,
        dailyTarget: Int
    ): GoalStatisticsSummary {
        val achievedDays = achievedDates.size
        return GoalStatisticsSummary(
            achievedDays = achievedDays,
            totalDays = dailyStatistics.size,
            targetDifference = dailyStatistics.sumOf { it.consumedCalories } -
                dailyTarget * dailyStatistics.size,
            achievementRate = percentage(achievedDays, dailyStatistics.size),
            longestStreak = longestConsecutiveStreak(achievedDates),
            firstAchievedDate = achievedDates.firstOrNull(),
            lastAchievedDate = achievedDates.lastOrNull()
        )
    }

    private fun DailyStatistics.isGoalAchieved(target: Int, goal: Goal): Boolean =
        consumedCalories > 0 &&
            target > 0 &&
            evaluateDailyGoal(consumedCalories, target, goal).status == DailyCalorieStatus.GOOD

    private fun List<DailyStatistics>.toCalorieSummary(
        selector: (DailyStatistics) -> Int
    ): CalorieStatisticsSummary {
        val total = sumOf(selector)
        val highestDay = filter { selector(it) > 0 }.maxByOrNull(selector)
        return CalorieStatisticsSummary(
            total = total,
            dailyAverage = if (isEmpty()) 0 else total / size,
            highest = DatedStatistics(
                value = highestDay?.let(selector) ?: 0,
                date = highestDay?.date
            )
        )
    }

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(clock.zone).toLocalDate()
}

private fun datesBetween(start: LocalDate, end: LocalDate): List<LocalDate> =
    generateSequence(start) { current ->
        current.plusDays(1).takeUnless { it.isAfter(end) }
    }.toList()

private fun percentage(value: Int, total: Int): Int =
    if (total == 0) 0 else (value * 100f / total).roundToInt()

private fun longestConsecutiveStreak(dates: List<LocalDate>): GoalStreak {
    var best = GoalStreak()
    var currentStart: LocalDate? = null
    var previous: LocalDate? = null

    dates.forEach { date ->
        currentStart = if (previous?.plusDays(1) == date) currentStart else date
        val length = currentStart?.let { start ->
            (date.toEpochDay() - start.toEpochDay() + 1).toInt()
        } ?: 0
        if (length > best.length) {
            best = GoalStreak(length, currentStart, date)
        }
        previous = date
    }
    return best
}
