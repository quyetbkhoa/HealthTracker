package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveProfileUseCaseTest {
    private val instant = Instant.parse("2026-07-22T03:00:00Z")
    private val clock = Clock.fixed(instant, ZoneOffset.UTC)
    private val repository = FakeSaveProfileRepository()
    private val useCase = SaveProfileUseCase(repository, CalculateTdeeUseCase(), clock)

    @Test
    fun `new profile receives calorie values and tracking start`() = runTest {
        useCase(validProfile())

        val saved = checkNotNull(repository.savedProfile)
        assertTrue(saved.bmrCalories > 0)
        assertTrue(saved.tdeeCalories > 0)
        assertTrue(saved.dailyCalorieTarget > 0)
        assertEquals(instant.toEpochMilli(), saved.activityTrackingStartedAt)
    }

    @Test
    fun `existing tracking start is preserved`() = runTest {
        val originalTrackingStart = instant.minusSeconds(86_400L * 20).toEpochMilli()

        useCase(validProfile().copy(activityTrackingStartedAt = originalTrackingStart))

        assertEquals(originalTrackingStart, repository.savedProfile?.activityTrackingStartedAt)
    }

    private fun validProfile() = UserProfile(
        fullName = "Tester",
        dateOfBirth = LocalDate.of(1995, 6, 15)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli(),
        weightKg = 70f,
        heightCm = 175f
    )
}

private class FakeSaveProfileRepository : ProfileRepository {
    override val userProfile = MutableStateFlow<UserProfile?>(null)
    var savedProfile: UserProfile? = null

    override suspend fun saveProfile(profile: UserProfile) {
        savedProfile = profile
        userProfile.value = profile
    }

    override suspend fun clearProfile() {
        savedProfile = null
        userProfile.value = null
    }
}
