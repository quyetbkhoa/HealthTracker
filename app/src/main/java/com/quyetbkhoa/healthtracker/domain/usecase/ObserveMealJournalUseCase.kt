package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class MealJournalData(
    val targetCalories: Int,
    val meals: List<MealEntry>
)

class ObserveMealJournalUseCase @Inject constructor(
    private val mealRepository: MealRepository,
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(epochDay: Long, languageTag: String): Flow<MealJournalData> = combine(
        mealRepository.observeMealsByDay(epochDay, languageTag),
        profileRepository.userProfile
    ) { meals, profile ->
        MealJournalData(
            targetCalories = profile?.dailyCalorieTarget ?: 0,
            meals = meals
        )
    }
}
