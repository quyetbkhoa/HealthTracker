package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.repository.DailyCalorieRepository
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import com.quyetbkhoa.healthtracker.domain.repository.ReminderScheduler
import com.quyetbkhoa.healthtracker.domain.repository.SettingsRepository
import javax.inject.Inject

class ResetUserDataUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val dailyCalorieRepository: DailyCalorieRepository,
    private val mealRepository: MealRepository,
    private val activityRepository: ActivityRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke() {
        settingsRepository.setRemindersEnabled(false)
        reminderScheduler.cancelAll()
        activityRepository.clearActivityRecords()
        mealRepository.clearMeals()
        dailyCalorieRepository.clear()
        profileRepository.clearProfile()
    }
}
