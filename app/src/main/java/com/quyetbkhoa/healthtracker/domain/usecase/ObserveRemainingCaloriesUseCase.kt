package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveRemainingCaloriesUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val mealRepository: MealRepository,
    private val clock: Clock
) {
    operator fun invoke(languageTag: String): Flow<Int?> {
        val today = LocalDate.now(clock).toEpochDay()
        return combine(
            profileRepository.userProfile,
            mealRepository.observeMealsByDay(today, languageTag)
        ) { profile, meals ->
            profile?.dailyCalorieTarget?.minus(meals.sumOf(MealEntry::calories))
        }
    }
}
