package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import javax.inject.Inject

enum class AddMealValidationError {
    EMPTY_NAME,
    INVALID_CALORIES,
    CALORIES_TOO_HIGH
}

sealed interface AddMealResult {
    data object Success : AddMealResult
    data class Invalid(val error: AddMealValidationError) : AddMealResult
}

class AddMealUseCase @Inject constructor(
    private val mealRepository: MealRepository
) {
    suspend operator fun invoke(meal: MealEntry): AddMealResult {
        if (meal.name.isBlank()) {
            return AddMealResult.Invalid(AddMealValidationError.EMPTY_NAME)
        }
        if (meal.calories <= 0) {
            return AddMealResult.Invalid(AddMealValidationError.INVALID_CALORIES)
        }
        if (meal.calories > MAX_CALORIES_PER_MEAL) {
            return AddMealResult.Invalid(AddMealValidationError.CALORIES_TOO_HIGH)
        }

        mealRepository.addMeal(meal.copy(name = meal.name.trim()))
        return AddMealResult.Success
    }

    private companion object {
        const val MAX_CALORIES_PER_MEAL = 10_000
    }
}
