package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class SaveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val calculateTdee: CalculateTdeeUseCase,
    private val clock: Clock
) {
    suspend operator fun invoke(profile: UserProfile) {
        val tdee = calculateTdee(profile, LocalDate.now(clock))
        profileRepository.saveProfile(
            profile.copy(
                bmrCalories = tdee.bmrCalories,
                tdeeCalories = tdee.tdeeCalories,
                dailyCalorieTarget = tdee.targetCalories,
                activityTrackingStartedAt = profile.activityTrackingStartedAt
                    .takeIf { it > 0L }
                    ?: clock.millis()
            )
        )
    }
}
