package com.quyetbkhoa.healthtracker.data.seed

import com.quyetbkhoa.healthtracker.data.local.activity.ActivityDao
import com.quyetbkhoa.healthtracker.data.local.activity.ActivityRecordEntity
import com.quyetbkhoa.healthtracker.data.local.meal.MealDao
import com.quyetbkhoa.healthtracker.data.local.meal.MealEntity
import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import com.quyetbkhoa.healthtracker.domain.model.Gender
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.domain.usecase.CalculateTdeeUseCase
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/** Inserts a realistic three-month journal for a fresh local installation. */
@Singleton
class DemoDataInitializer @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val mealDao: MealDao,
    private val activityDao: ActivityDao,
    private val calculateTdee: CalculateTdeeUseCase
) {
    suspend fun seedIfEmpty() {
        if (profileRepository.userProfile.first() != null ||
            mealDao.count() > 0 || activityDao.countActivityRecords() > 0
        ) return

        val today = LocalDate.now()
        val profileWithoutCalories = UserProfile(
            fullName = "Quyết",
            dateOfBirth = LocalDate.of(2002, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            gender = Gender.MALE,
            weightKg = 75f,
            heightCm = 175f,
            activityLevel = ActivityLevel.MODERATE,
            goal = Goal.MAINTAIN,
            activityTrackingStartedAt = today.minusDays(HISTORY_DAYS - 1L)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        val calories = calculateTdee(profileWithoutCalories, today)
        profileRepository.saveProfile(
            profileWithoutCalories.copy(
                bmrCalories = calories.bmrCalories,
                tdeeCalories = calories.tdeeCalories,
                dailyCalorieTarget = calories.targetCalories
            )
        )

        (0 until HISTORY_DAYS).forEach { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val variation = ((daysAgo * 37) % 181) - 90
            seedMeals(date, calories.targetCalories + variation)
            seedWorkout(date, daysAgo)
        }
    }

    private suspend fun seedMeals(date: LocalDate, dailyCalories: Int) {
        val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val meals = listOf(
            MealSeed("Yến mạch, chuối và sữa chua", "BREAKFAST", 550, 7, 30),
            MealSeed("Cơm gà áp chảo và rau xanh", "LUNCH", 850, 12, 15),
            MealSeed("Cá hồi, khoai lang và salad", "DINNER", 950, 19, 0),
            MealSeed("Táo và hạnh nhân", "SNACK", 320, 15, 45)
        )
        val baseTotal = meals.sumOf { it.calories }
        meals.forEachIndexed { index, meal ->
            val adjustedCalories = meal.calories + if (index == 1) dailyCalories - baseTotal else 0
            mealDao.insert(
                MealEntity(
                    foodId = null,
                    nameSnapshot = meal.name,
                    calories = adjustedCalories,
                    mealType = meal.type,
                    consumedGrams = adjustedCalories.toDouble(),
                    caloriesPer100GramsSnapshot = 100.0,
                    eatenAt = dayStart + (meal.hour * 60L + meal.minute) * 60_000L
                )
            )
        }
    }

    private suspend fun seedWorkout(date: LocalDate, daysAgo: Int) {
        val workout = when (daysAgo % 7) {
            0, 3 -> WorkoutSeed(activityTypeId = 10, durationMinutes = 50, met = 5.0)
            1 -> WorkoutSeed(activityTypeId = 3, durationMinutes = 35, met = 7.0)
            5 -> WorkoutSeed(activityTypeId = 14, durationMinutes = 60, met = 5.5)
            else -> null
        } ?: return
        val performedAt = date.atTime(18, 30).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        activityDao.insertActivityRecord(
            ActivityRecordEntity(
                activityTypeId = workout.activityTypeId,
                durationMinutes = workout.durationMinutes,
                metAtCreation = workout.met,
                weightKgAtCreation = 75.0,
                caloriesBurned = (workout.met * 75.0 * workout.durationMinutes / 60.0).roundToInt().toDouble(),
                performedAt = performedAt,
                createdAt = performedAt
            )
        )
    }

    private data class MealSeed(val name: String, val type: String, val calories: Int, val hour: Int, val minute: Int)
    private data class WorkoutSeed(val activityTypeId: Long, val durationMinutes: Int, val met: Double)

    private companion object {
        const val HISTORY_DAYS = 90
    }
}
