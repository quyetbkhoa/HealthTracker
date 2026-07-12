package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.repository.DailyCalorieRepository
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import javax.inject.Inject

class ResetUserDataUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dailyCalorieRepository: DailyCalorieRepository,
    private val mealRepository: MealRepository,
    private val activityRepository: ActivityRepository
) {
    suspend operator fun invoke() {
        activityRepository.clearActivityRecords()
        mealRepository.clearMeals()
        dailyCalorieRepository.clear()
        profileRepository.clearProfile()
    }
}
