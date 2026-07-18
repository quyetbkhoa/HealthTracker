package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.PhysicalActivityRecord
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class StatisticsData(
    val profile: UserProfile?,
    val meals: List<MealEntry>,
    val activities: List<PhysicalActivityRecord>
)

class ObserveStatisticsDataUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val mealRepository: MealRepository,
    private val activityRepository: ActivityRepository
) {
    operator fun invoke(
        startMillis: Long,
        endMillis: Long,
        languageTag: String
    ): Flow<StatisticsData> = combine(
        profileRepository.userProfile,
        mealRepository.observeMealsBetween(startMillis, endMillis, languageTag),
        activityRepository.observeRecordsBetween(startMillis, endMillis)
    ) { profile, meals, activities ->
        StatisticsData(profile, meals, activities)
    }
}
