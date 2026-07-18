package com.quyetbkhoa.healthtracker.presentation.statistics

import com.quyetbkhoa.healthtracker.domain.model.StatisticsRange

import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.PhysicalActivityRecord
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieStatus
import com.quyetbkhoa.healthtracker.domain.usecase.EvaluateDailyCalorieGoalUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.roundToInt

internal data class StatisticsInput(
    val range: StatisticsRange,
    val requestedStartDate: LocalDate,
    val dailyTarget: Int,
    val goal: Goal,
    val meals: List<MealEntry>,
    val activities: List<PhysicalActivityRecord>
)

class StatisticsCalculator @Inject constructor(
    private val evaluateDailyGoal: EvaluateDailyCalorieGoalUseCase,
    clock: Clock
) {
    private val zone: ZoneId = clock.zone
    private val today: LocalDate = LocalDate.now(clock)

    internal fun calculate(input: StatisticsInput): StatisticsUiState {
        val daily = buildDailyStatistics(input)
        val achievedDates = daily.filter { it.isGoalAchieved(input.dailyTarget, input.goal) }.map { it.date }

        return StatisticsUiState(
            isLoading = false,
            selectedRange = input.range,
            dailyTarget = input.dailyTarget,
            consumed = daily.toCalorieStatistics { it.consumedCalories },
            burned = daily.toCalorieStatistics { it.burnedCalories },
            goal = buildGoalStatistics(daily, achievedDates, input.dailyTarget),
            dailyStatistics = daily
        )
    }

    private fun buildDailyStatistics(input: StatisticsInput): List<DailyStatistic> {
        val mealsByDate = input.meals.groupBy { it.eatenAt.toLocalDate() }
        val activitiesByDate = input.activities.groupBy { it.performedAt.toLocalDate() }
        val firstRecordedDate = (mealsByDate.keys + activitiesByDate.keys).minOrNull()
        val startDate = if (input.range == StatisticsRange.ALL) firstRecordedDate ?: today else input.requestedStartDate

        return datesBetween(startDate, today).map { date ->
            DailyStatistic(
                date = date,
                consumedCalories = mealsByDate[date].orEmpty().sumOf(MealEntry::calories),
                burnedCalories = activitiesByDate[date].orEmpty().sumOf { it.caloriesBurned }.roundToInt()
            )
        }
    }

    private fun buildGoalStatistics(
        daily: List<DailyStatistic>,
        achievedDates: List<LocalDate>,
        dailyTarget: Int
    ): GoalStatistics {
        val achievedDays = achievedDates.size
        return GoalStatistics(
            achievedDays = achievedDays,
            totalDays = daily.size,
            targetDifference = daily.sumOf { it.consumedCalories } - dailyTarget * daily.size,
            achievementRate = percentage(achievedDays, daily.size),
            longestStreak = longestConsecutiveStreak(achievedDates),
            firstAchievedDate = achievedDates.firstOrNull(),
            lastAchievedDate = achievedDates.lastOrNull()
        )
    }

    private fun DailyStatistic.isGoalAchieved(target: Int, goal: Goal): Boolean =
        consumedCalories > 0 && target > 0 &&
            evaluateDailyGoal(consumedCalories, target, goal).status == DailyCalorieStatus.GOOD

    private fun List<DailyStatistic>.toCalorieStatistics(selector: (DailyStatistic) -> Int): CalorieStatistics {
        val total = sumOf(selector)
        val highestDay = filter { selector(it) > 0 }.maxByOrNull(selector)
        return CalorieStatistics(
            total = total,
            dailyAverage = if (isEmpty()) 0 else total / size,
            highest = DatedStatistic(highestDay?.let(selector) ?: 0, highestDay?.date)
        )
    }

    private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
}

private fun datesBetween(start: LocalDate, end: LocalDate): List<LocalDate> =
    generateSequence(start) { current -> current.plusDays(1).takeUnless { it.isAfter(end) } }.toList()

private fun percentage(value: Int, total: Int): Int =
    if (total == 0) 0 else (value * 100f / total).roundToInt()

private fun longestConsecutiveStreak(dates: List<LocalDate>): GoalStreak {
    var best = GoalStreak()
    var currentStart: LocalDate? = null
    var previous: LocalDate? = null

    dates.forEach { date ->
        currentStart = if (previous?.plusDays(1) == date) currentStart else date
        val length = currentStart?.let { start -> (date.toEpochDay() - start.toEpochDay() + 1).toInt() } ?: 0
        if (length > best.length) best = GoalStreak(length, currentStart, date)
        previous = date
    }
    return best
}
