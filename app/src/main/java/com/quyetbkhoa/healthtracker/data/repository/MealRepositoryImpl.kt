package com.quyetbkhoa.healthtracker.data.repository

import com.quyetbkhoa.healthtracker.data.local.meal.MealDao
import com.quyetbkhoa.healthtracker.data.local.meal.MealEntity
import com.quyetbkhoa.healthtracker.data.local.meal.LocalizedMealRow
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class MealRepositoryImpl @Inject constructor(
    private val mealDao: MealDao
) : MealRepository {
    override fun observeMealsByDay(epochDay: Long, languageTag: String): Flow<List<MealEntry>> {
        val zoneId = ZoneId.systemDefault()
        val date = LocalDate.ofEpochDay(epochDay)
        val startMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        return observeMealsBetween(startMillis, endMillis, languageTag)
    }

    override fun observeMealsBetween(
        startMillis: Long,
        endMillis: Long,
        languageTag: String
    ): Flow<List<MealEntry>> = mealDao.observeMealsBetween(startMillis, endMillis, languageTag)
        .map { rows -> rows.map(LocalizedMealRow::toDomain) }

    override suspend fun addMeal(meal: MealEntry) {
        mealDao.insert(meal.toEntity())
    }

    override suspend fun updateMeal(meal: MealEntry) {
        mealDao.update(meal.toEntity())
    }

    override suspend fun deleteMeal(id: Long) {
        mealDao.deleteById(id)
    }

    override suspend fun clearMeals() {
        mealDao.deleteAll()
    }
}

private fun LocalizedMealRow.toDomain(): MealEntry = MealEntry(
    id = id,
    foodId = foodId,
    name = displayName,
    nameSnapshot = nameSnapshot,
    calories = calories,
    mealType = MealType.valueOf(mealType),
    consumedGrams = consumedGrams,
    caloriesPer100GramsSnapshot = caloriesPer100GramsSnapshot,
    eatenAt = eatenAt
)

private fun MealEntry.toEntity(): MealEntity = MealEntity(
    id = id,
    foodId = foodId,
    nameSnapshot = nameSnapshot,
    calories = calories,
    mealType = mealType.name,
    consumedGrams = consumedGrams,
    caloriesPer100GramsSnapshot = caloriesPer100GramsSnapshot,
    eatenAt = eatenAt
)
