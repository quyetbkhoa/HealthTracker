package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.repository.DailyCalorieRepository
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import javax.inject.Inject

class ResetUserDataUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dailyCalorieRepository: DailyCalorieRepository,
    private val mealRepository: MealRepository
) {
    suspend operator fun invoke() {
        mealRepository.clearMeals()
        dailyCalorieRepository.clear()
        profileRepository.clearProfile()
    }
}
