package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import javax.inject.Inject

enum class AddMealValidationError {
    EMPTY_NAME,
    INVALID_GRAMS,
    INVALID_CALORIES_PER_100_GRAMS,
    CALORIES_TOO_HIGH
}

sealed interface AddMealResult {
    data object Success : AddMealResult
    data class Invalid(val error: AddMealValidationError) : AddMealResult
}

class AddMealUseCase @Inject constructor(
    private val mealRepository: MealRepository,
    private val calculateMealCalories: CalculateMealCaloriesUseCase
) {
    suspend operator fun invoke(meal: MealEntry): AddMealResult {
        if (meal.nameSnapshot.isBlank()) {
            return AddMealResult.Invalid(AddMealValidationError.EMPTY_NAME)
        }
        if (!meal.consumedGrams.isFinite() || meal.consumedGrams <= 0.0) {
            return AddMealResult.Invalid(AddMealValidationError.INVALID_GRAMS)
        }
        if (!meal.caloriesPer100GramsSnapshot.isFinite() ||
            meal.caloriesPer100GramsSnapshot <= 0.0
        ) {
            return AddMealResult.Invalid(AddMealValidationError.INVALID_CALORIES_PER_100_GRAMS)
        }
        val calories = calculateMealCalories(
            caloriesPer100Grams = meal.caloriesPer100GramsSnapshot,
            consumedGrams = meal.consumedGrams
        ) ?: return AddMealResult.Invalid(AddMealValidationError.INVALID_CALORIES_PER_100_GRAMS)
        if (calories > MAX_CALORIES_PER_MEAL) {
            return AddMealResult.Invalid(AddMealValidationError.CALORIES_TOO_HIGH)
        }

        val trimmedName = meal.nameSnapshot.trim()
        mealRepository.addMeal(
            meal.copy(
                name = trimmedName,
                nameSnapshot = trimmedName,
                calories = calories
            )
        )
        return AddMealResult.Success
    }

    private companion object {
        const val MAX_CALORIES_PER_MEAL = 10_000
    }
}
