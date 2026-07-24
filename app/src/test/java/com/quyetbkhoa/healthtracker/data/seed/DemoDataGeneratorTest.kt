package com.quyetbkhoa.healthtracker.data.seed

import com.quyetbkhoa.healthtracker.data.local.activity.DefaultActivities
import com.quyetbkhoa.healthtracker.data.local.food.DefaultFoods
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoDataGeneratorTest {
    private val today = LocalDate.of(2026, 7, 24)
    private val zone = ZoneOffset.UTC
    private val profile = UserProfile(
        weightKg = 72f,
        dailyCalorieTarget = 2_200
    )

    @Test
    fun `generates latest 60 days from preset foods with varied calorie cases`() {
        val batch = DemoDataGenerator.generate(profile, today, zone)
        val allowedFoodIds = DefaultFoods.foods.map { it.id }.toSet()
        val mealsByDate = batch.meals.groupBy {
            java.time.Instant.ofEpochMilli(it.eatenAt).atZone(zone).toLocalDate()
        }
        val totalsByDate = (0 until DemoDataGenerator.HISTORY_DAYS).map { dayIndex ->
            val date = batch.startDate.plusDays(dayIndex.toLong())
            mealsByDate[date].orEmpty().sumOf { it.calories }
        }

        assertEquals(today.minusDays(59), batch.startDate)
        assertEquals(54, mealsByDate.size)
        assertTrue(batch.meals.all { it.foodId in allowedFoodIds })
        assertTrue(batch.meals.all { it.consumedGrams > 0 && it.calories > 0 })
        assertTrue(totalsByDate.any { it == 0 })
        assertTrue(totalsByDate.filter { it > 0 }.min() < profile.dailyCalorieTarget * 0.7)
        assertTrue(totalsByDate.max() > profile.dailyCalorieTarget * 1.3)
        assertTrue(totalsByDate.any {
            it in (profile.dailyCalorieTarget * 0.93).toInt()..
                (profile.dailyCalorieTarget * 1.07).toInt()
        })
    }

    @Test
    fun `generates rest light intense and double workout days`() {
        val batch = DemoDataGenerator.generate(profile, today, zone)
        val allowedActivityIds = DefaultActivities.values.map { it.id }.toSet()
        val activitiesByDate = batch.activities.groupBy {
            java.time.Instant.ofEpochMilli(it.performedAt).atZone(zone).toLocalDate()
        }
        val allDates = (0 until DemoDataGenerator.HISTORY_DAYS).map {
            batch.startDate.plusDays(it.toLong())
        }

        assertTrue(batch.activities.all { it.activityTypeId in allowedActivityIds })
        assertTrue(batch.activities.all { it.durationMinutes >= 10 && it.caloriesBurned > 0 })
        assertEquals(12, allDates.count { activitiesByDate[it].isNullOrEmpty() })
        assertEquals(6, activitiesByDate.values.count { it.size == 2 })
        assertTrue(batch.activities.map { it.activityTypeId }.distinct().size >= 8)
        assertTrue(batch.activities.minOf { it.caloriesBurned } < 100)
        assertTrue(batch.activities.maxOf { it.caloriesBurned } > 500)
    }
}
