package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import javax.inject.Inject

class DeleteMealUseCase @Inject constructor(
    private val mealRepository: MealRepository
) {
    suspend operator fun invoke(id: Long) {
        if (id > 0L) mealRepository.deleteMeal(id)
    }
}
