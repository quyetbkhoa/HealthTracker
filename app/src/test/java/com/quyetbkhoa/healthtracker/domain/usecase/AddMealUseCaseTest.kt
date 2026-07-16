package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddMealUseCaseTest {
    private val repository = FakeMealRepository()
    private val useCase = AddMealUseCase(repository, CalculateMealCaloriesUseCase())

    @Test
    fun `blank name returns validation error`() = runBlocking {
        val result = useCase(meal(name = "   ", calories = 500))

        assertEquals(
            AddMealResult.Invalid(AddMealValidationError.EMPTY_NAME),
            result
        )
        assertNull(repository.addedMeal)
    }

    @Test
    fun `invalid grams returns validation error`() = runBlocking {
        val result = useCase(meal(name = "Cơm gà", calories = 0, grams = 0.0))

        assertEquals(
            AddMealResult.Invalid(AddMealValidationError.INVALID_GRAMS),
            result
        )
        assertNull(repository.addedMeal)
    }

    @Test
    fun `valid meal is trimmed and saved`() = runBlocking {
        val result = useCase(meal(name = "  Cơm gà  ", calories = 500))

        assertEquals(AddMealResult.Success, result)
        assertEquals("Cơm gà", repository.addedMeal?.name)
        assertEquals(500, repository.addedMeal?.calories)
    }

    private fun meal(name: String, calories: Int, grams: Double = 100.0) = MealEntry(
        name = name,
        calories = calories,
        mealType = MealType.LUNCH,
        consumedGrams = grams,
        caloriesPer100GramsSnapshot = calories.toDouble(),
        eatenAt = 0L
    )
}

private class FakeMealRepository : MealRepository {
    override fun observeMealsBetween(startMillis: Long, endMillis: Long, languageTag: String) =
        kotlinx.coroutines.flow.flowOf(emptyList<MealEntry>())
    var addedMeal: MealEntry? = null

    override fun observeMealsByDay(
        epochDay: Long,
        languageTag: String
    ): Flow<List<MealEntry>> = emptyFlow()

    override suspend fun addMeal(meal: MealEntry) {
        addedMeal = meal
    }

    override suspend fun updateMeal(meal: MealEntry) = Unit

    override suspend fun deleteMeal(id: Long) = Unit

    override suspend fun clearMeals() = Unit
}
