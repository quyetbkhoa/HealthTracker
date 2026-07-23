package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.PhysicalActivityRecord
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

enum class AddActivityRecordError {
    INVALID_ACTIVITY,
    INVALID_DURATION,
    INVALID_WEIGHT
}

sealed interface AddActivityRecordResult {
    data object Success : AddActivityRecordResult
    data class Invalid(val error: AddActivityRecordError) : AddActivityRecordResult
}

class AddActivityRecordUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val calculateCalories: CalculateActivityCaloriesUseCase,
    private val clock: Clock
) {
    suspend operator fun invoke(
        activityTypeId: Long,
        met: Double,
        weightKg: Double,
        durationMinutes: Int,
        epochDay: Long = LocalDate.now(clock).toEpochDay()
    ): AddActivityRecordResult {
        if (activityTypeId <= 0L || !met.isFinite() || met <= 0.0) {
            return AddActivityRecordResult.Invalid(AddActivityRecordError.INVALID_ACTIVITY)
        }
        if (durationMinutes !in MIN_DURATION_MINUTES..MAX_DURATION_MINUTES) {
            return AddActivityRecordResult.Invalid(AddActivityRecordError.INVALID_DURATION)
        }
        if (!weightKg.isFinite() || weightKg !in MIN_WEIGHT_KG..MAX_WEIGHT_KG) {
            return AddActivityRecordResult.Invalid(AddActivityRecordError.INVALID_WEIGHT)
        }
        val calories = calculateCalories(met, weightKg, durationMinutes)
        if (calories <= 0.0) {
            return AddActivityRecordResult.Invalid(AddActivityRecordError.INVALID_ACTIVITY)
        }
        val now = clock.millis()
        val localTime = Instant.ofEpochMilli(now).atZone(clock.zone).toLocalTime()
        val performedAt = LocalDate.ofEpochDay(epochDay)
            .atTime(localTime)
            .atZone(clock.zone)
            .toInstant()
            .toEpochMilli()
        activityRepository.addActivityRecord(
            PhysicalActivityRecord(
                activityTypeId = activityTypeId,
                durationMinutes = durationMinutes,
                metAtCreation = met,
                weightKgAtCreation = weightKg,
                caloriesBurned = calories,
                performedAt = performedAt,
                createdAt = now
            )
        )
        return AddActivityRecordResult.Success
    }

    private companion object {
        const val MIN_DURATION_MINUTES = 1
        const val MAX_DURATION_MINUTES = 600
        const val MIN_WEIGHT_KG = 1.0
        const val MAX_WEIGHT_KG = 300.0
    }
}

class EnsureDefaultActivitiesUseCase @Inject constructor(
    private val activityRepository: ActivityRepository
) {
    suspend operator fun invoke() = activityRepository.seedDefaultActivities()
}

class SetActivityFavoriteUseCase @Inject constructor(
    private val activityRepository: ActivityRepository
) {
    suspend operator fun invoke(activityId: Long, isFavorite: Boolean) {
        if (activityId > 0L) activityRepository.setFavorite(activityId, isFavorite)
    }
}

class DeleteActivityRecordUseCase @Inject constructor(
    private val activityRepository: ActivityRepository
) {
    suspend operator fun invoke(recordId: Long) {
        if (recordId > 0L) activityRepository.deleteActivityRecord(recordId)
    }
}
