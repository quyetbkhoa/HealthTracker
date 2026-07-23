package com.quyetbkhoa.healthtracker.data.repository

import androidx.room.withTransaction
import com.quyetbkhoa.healthtracker.data.local.HealthTrackerDatabase
import com.quyetbkhoa.healthtracker.data.seed.DemoDataGenerator
import com.quyetbkhoa.healthtracker.domain.repository.DemoDataRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class DemoDataRepositoryImpl @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val database: HealthTrackerDatabase,
    private val clock: Clock
) : DemoDataRepository {
    override suspend fun replaceWithDemoData() {
        val profile = checkNotNull(profileRepository.userProfile.first()) {
            "A user profile is required before demo data can be generated."
        }
        val batch = DemoDataGenerator.generate(
            profile = profile,
            today = LocalDate.now(clock),
            zoneId = clock.zone
        )

        database.withTransaction {
            database.mealDao().deleteAll()
            database.activityDao().deleteAllActivityRecords()
            database.mealDao().insertAll(batch.meals)
            database.activityDao().insertActivityRecords(batch.activities)
        }
        profileRepository.saveProfile(
            profile.copy(
                activityTrackingStartedAt = batch.startDate
                    .atStartOfDay(clock.zone)
                    .toInstant()
                    .toEpochMilli()
            )
        )
    }
}
