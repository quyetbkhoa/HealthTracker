package com.quyetbkhoa.healthtracker.data.seed

import androidx.room.withTransaction
import com.quyetbkhoa.healthtracker.data.local.HealthTrackerDatabase
import com.quyetbkhoa.healthtracker.data.local.activity.ActivityRecordEntity
import com.quyetbkhoa.healthtracker.data.local.activity.DefaultActivities
import com.quyetbkhoa.healthtracker.data.local.food.DefaultFood
import com.quyetbkhoa.healthtracker.data.local.food.DefaultFoods
import com.quyetbkhoa.healthtracker.data.local.meal.MealEntity
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first

/**
 * Replaces journal records with a deterministic, varied two-month data set.
 *
 * Profile information is preserved. Only the internal activity tracking start date is moved
 * backwards so activity-level suggestions can evaluate the generated history correctly.
 */
@Singleton
class DemoDataInitializer @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val database: HealthTrackerDatabase,
    private val clock: Clock
) {
    suspend fun replaceWithTwoMonthDemo(): DemoDataSummary {
        val profile = checkNotNull(profileRepository.userProfile.first()) {
            "A user profile is required before demo data can be generated."
        }
        val batch = DemoDataGenerator.generate(
            profile = profile,
            today = LocalDate.now(clock),
            zoneId = clock.zone
        )

        database.withTransaction {
            database.mealDao().deleteAll()
            database.activityDao().deleteAllActivityRecords()
            database.mealDao().insertAll(batch.meals)
            database.activityDao().insertActivityRecords(batch.activities)
        }
        profileRepository.saveProfile(
            profile.copy(
                activityTrackingStartedAt = batch.startDate
                    .atStartOfDay(clock.zone)
                    .toInstant()
                    .toEpochMilli()
            )
        )

        return DemoDataSummary(
            dayCount = DemoDataGenerator.HISTORY_DAYS,
            mealCount = batch.meals.size,
            activityCount = batch.activities.size
        )
    }
}

data class DemoDataSummary(
    val dayCount: Int,
    val mealCount: Int,
    val activityCount: Int
)

internal data class DemoDataBatch(
    val startDate: LocalDate,
    val meals: List<MealEntity>,
    val activities: List<ActivityRecordEntity>
)

internal object DemoDataGenerator {
    const val HISTORY_DAYS = 60

    private val breakfastTemplates = listOf(
        listOf(portion(27, 55.0), portion(14, 150.0), portion(21, 110.0)),
        listOf(portion(3, 90.0), portion(13, 60.0), portion(15, 250.0)),
        listOf(portion(34, 250.0), portion(23, 180.0))
    )
    private val lunchTemplates = listOf(
        listOf(portion(1, 220.0), portion(7, 160.0), portion(18, 150.0)),
        listOf(portion(6, 330.0), portion(17, 120.0)),
        listOf(portion(2, 450.0)),
        listOf(portion(4, 350.0)),
        listOf(portion(1, 180.0), portion(9, 140.0), portion(19, 180.0))
    )
    private val dinnerTemplates = listOf(
        listOf(portion(28, 250.0), portion(10, 160.0), portion(20, 150.0)),
        listOf(portion(1, 180.0), portion(11, 180.0), portion(33, 300.0)),
        listOf(portion(35, 320.0), portion(17, 100.0)),
        listOf(portion(16, 180.0), portion(1, 180.0), portion(18, 200.0)),
        listOf(portion(29, 220.0), portion(8, 150.0), portion(17, 100.0))
    )
    private val snackTemplates = listOf(
        listOf(portion(22, 150.0), portion(30, 25.0)),
        listOf(portion(24, 200.0), portion(14, 120.0)),
        listOf(portion(25, 300.0), portion(31, 25.0)),
        listOf(portion(26, 100.0), portion(32, 20.0))
    )

    fun generate(
        profile: UserProfile,
        today: LocalDate,
        zoneId: ZoneId
    ): DemoDataBatch {
        val startDate = today.minusDays(HISTORY_DAYS - 1L)
        val meals = buildList {
            repeat(HISTORY_DAYS) { dayIndex ->
                val date = startDate.plusDays(dayIndex.toLong())
                addAll(generateMealsForDay(profile, date, dayIndex, zoneId))
            }
        }
        val activities = buildList {
            repeat(HISTORY_DAYS) { dayIndex ->
                val date = startDate.plusDays(dayIndex.toLong())
                addAll(generateActivitiesForDay(profile, date, dayIndex, zoneId))
            }
        }
        return DemoDataBatch(startDate, meals, activities)
    }

    private fun generateMealsForDay(
        profile: UserProfile,
        date: LocalDate,
        dayIndex: Int,
        zoneId: ZoneId
    ): List<MealEntity> {
        val scenario = foodScenario(dayIndex)
        if (scenario.isEmptyDay) return emptyList()

        val selectedMeals = buildList {
            if (scenario.hasBreakfast) {
                add(MealPlan(MealType.BREAKFAST, 7, 10, breakfastTemplates[dayIndex % breakfastTemplates.size]))
            }
            if (scenario.hasLunch) {
                add(MealPlan(MealType.LUNCH, 12, 5, lunchTemplates[dayIndex % lunchTemplates.size]))
            }
            if (scenario.hasDinner) {
                val dinnerHour = if (scenario.isLateDinner) 22 else 19
                add(MealPlan(MealType.DINNER, dinnerHour, 0, dinnerTemplates[dayIndex % dinnerTemplates.size]))
            }
            repeat(scenario.snackCount) { snackIndex ->
                add(
                    MealPlan(
                        type = MealType.SNACK,
                        hour = if (snackIndex == 0) 15 else 21,
                        minute = 30,
                        portions = snackTemplates[(dayIndex + snackIndex) % snackTemplates.size]
                    )
                )
            }
        }

        val baseCalories = selectedMeals.sumOf { plan ->
            plan.portions.sumOf(Portion::calories)
        }.coerceAtLeast(1)
        val targetCalories = profile.dailyCalorieTarget.takeIf { it > 0 } ?: DEFAULT_TARGET_CALORIES
        val deterministicJitter = (((dayIndex * 29) % 13) - 6) / 100.0
        val desiredCalories = targetCalories * (scenario.targetFactor + deterministicJitter)
        val scale = desiredCalories / baseCalories

        return selectedMeals.flatMap { plan ->
            plan.portions.mapIndexed { portionIndex, portion ->
                val grams = roundToFive(portion.grams * scale).coerceAtLeast(MIN_PORTION_GRAMS)
                val calories = (portion.food.entity.caloriesPer100Grams * grams / 100.0)
                    .roundToInt()
                    .coerceAtLeast(1)
                MealEntity(
                    foodId = portion.food.entity.id,
                    nameSnapshot = portion.food.vietnameseName,
                    calories = calories,
                    mealType = plan.type.name,
                    consumedGrams = grams,
                    caloriesPer100GramsSnapshot = portion.food.entity.caloriesPer100Grams,
                    eatenAt = date.atTime(plan.hour, plan.minute + portionIndex)
                        .atZone(zoneId)
                        .toInstant()
                        .toEpochMilli()
                )
            }
        }
    }

    private fun generateActivitiesForDay(
        profile: UserProfile,
        date: LocalDate,
        dayIndex: Int,
        zoneId: ZoneId
    ): List<ActivityRecordEntity> {
        val plans = when (dayIndex % 10) {
            0, 6 -> emptyList()
            1 -> listOf(ActivityPlan(1, 25, 7, 0))
            2 -> listOf(ActivityPlan(3, 35, 18, 15))
            3 -> listOf(ActivityPlan(10, 50, 18, 0), ActivityPlan(2, 20, 20, 0))
            4 -> listOf(ActivityPlan(11, 45, 6, 30))
            5 -> listOf(ActivityPlan(14, 60, 19, 0))
            7 -> listOf(ActivityPlan(6, 75, 17, 30))
            8 -> listOf(ActivityPlan(8, 25, 18, 30))
            else -> listOf(ActivityPlan(18, 50, 9, 0))
        }
        val weight = profile.weightKg.toDouble().takeIf { it > 0 } ?: DEFAULT_WEIGHT_KG
        val durationAdjustment = ((dayIndex * 7) % 11) - 5

        return plans.map { plan ->
            val activity = checkNotNull(DefaultActivities.values.find { it.id == plan.activityTypeId })
            val duration = (plan.durationMinutes + durationAdjustment).coerceAtLeast(10)
            val performedAt = date.atTime(plan.hour, plan.minute)
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli()
            ActivityRecordEntity(
                activityTypeId = activity.id,
                durationMinutes = duration,
                metAtCreation = activity.met,
                weightKgAtCreation = weight,
                caloriesBurned = (activity.met * weight * duration / 60.0)
                    .roundToInt()
                    .toDouble(),
                performedAt = performedAt,
                createdAt = performedAt
            )
        }
    }

    private fun foodScenario(dayIndex: Int): FoodScenario = when (dayIndex % 10) {
        0 -> FoodScenario(isEmptyDay = true)
        1 -> FoodScenario(targetFactor = 0.72, hasBreakfast = false)
        2 -> FoodScenario(targetFactor = 0.86, hasLunch = false)
        3 -> FoodScenario(targetFactor = 0.98)
        4 -> FoodScenario(targetFactor = 1.08, snackCount = 1)
        5 -> FoodScenario(targetFactor = 1.28, snackCount = 2)
        6 -> FoodScenario(targetFactor = 0.58, hasLunch = false)
        7 -> FoodScenario(targetFactor = 1.02, snackCount = 1)
        8 -> FoodScenario(targetFactor = 1.15, snackCount = 1)
        else -> FoodScenario(targetFactor = 1.42, snackCount = 2, isLateDinner = true)
    }

    private fun portion(foodId: Long, grams: Double): Portion {
        val food = checkNotNull(DefaultFoods.values.find { it.entity.id == foodId })
        return Portion(food, grams)
    }

    private fun roundToFive(value: Double): Double = (value / 5.0).roundToInt() * 5.0

    private data class Portion(val food: DefaultFood, val grams: Double) {
        val calories: Int
            get() = (food.entity.caloriesPer100Grams * grams / 100.0).roundToInt()
    }

    private data class MealPlan(
        val type: MealType,
        val hour: Int,
        val minute: Int,
        val portions: List<Portion>
    )

    private data class FoodScenario(
        val targetFactor: Double = 1.0,
        val hasBreakfast: Boolean = true,
        val hasLunch: Boolean = true,
        val hasDinner: Boolean = true,
        val snackCount: Int = 0,
        val isEmptyDay: Boolean = false,
        val isLateDinner: Boolean = false
    )

    private data class ActivityPlan(
        val activityTypeId: Long,
        val durationMinutes: Int,
        val hour: Int,
        val minute: Int
    )

    private const val DEFAULT_TARGET_CALORIES = 2_000
    private const val DEFAULT_WEIGHT_KG = 65.0
    private const val MIN_PORTION_GRAMS = 20.0
}
