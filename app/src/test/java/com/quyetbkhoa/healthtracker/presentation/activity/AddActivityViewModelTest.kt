package com.quyetbkhoa.healthtracker.presentation.activity

import com.quyetbkhoa.healthtracker.domain.model.PhysicalActivityRecord
import com.quyetbkhoa.healthtracker.domain.model.PhysicalActivityType
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.domain.usecase.AddActivityRecordUseCase
import com.quyetbkhoa.healthtracker.domain.usecase.CalculateActivityCaloriesUseCase
import com.quyetbkhoa.healthtracker.domain.usecase.EnsureDefaultActivitiesUseCase
import com.quyetbkhoa.healthtracker.domain.usecase.SetActivityFavoriteUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddActivityViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting activity collapses picker and duration updates calories`() = runTest {
        val repository = FakeActivityRepository()
        val viewModel = createViewModel(repository)
        val collection = backgroundScope.launch(dispatcher) { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.onAction(AddActivityAction.SelectActivity(1))
        assertEquals(1L, viewModel.uiState.value.selectedActivityId)
        assertFalse(viewModel.uiState.value.isActivityPickerExpanded)
        assertEquals(84.0, viewModel.uiState.value.estimatedCalories, 0.0001)

        viewModel.onAction(AddActivityAction.ChangeDuration(60))
        assertEquals(168.0, viewModel.uiState.value.estimatedCalories, 0.0001)

        viewModel.onAction(AddActivityAction.ReselectActivity)
        assertTrue(viewModel.uiState.value.isActivityPickerExpanded)
        collection.cancel()
    }

    @Test
    fun `changing activity and favorite updates state`() = runTest {
        val repository = FakeActivityRepository()
        val viewModel = createViewModel(repository)
        val collection = backgroundScope.launch(dispatcher) { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.onAction(AddActivityAction.SelectActivity(2))
        assertEquals(210.0, viewModel.uiState.value.estimatedCalories, 0.0001)

        viewModel.onAction(AddActivityAction.ToggleFavorite(1))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.activities.first { it.id == 1L }.isFavorite)
        assertEquals(1L, viewModel.uiState.value.activities.first().id)
        collection.cancel()
    }

    @Test
    fun `save creates one record and invalid state does not save`() = runTest {
        val repository = FakeActivityRepository()
        val viewModel = createViewModel(repository)
        val collection = backgroundScope.launch(dispatcher) { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.onAction(AddActivityAction.SaveActivity)
        assertTrue(repository.records.isEmpty())
        assertEquals(AddActivityError.INVALID_ACTIVITY, viewModel.uiState.value.error)

        val selectedDay = LocalDate.of(2025, 12, 25).toEpochDay()
        viewModel.onAction(AddActivityAction.SetDate(selectedDay))
        viewModel.onAction(AddActivityAction.SelectActivity(1))
        viewModel.onAction(AddActivityAction.SaveActivity)
        viewModel.onAction(AddActivityAction.SaveActivity)
        advanceUntilIdle()
        assertEquals(1, repository.records.size)
        assertEquals(84.0, repository.records.single().caloriesBurned, 0.0001)
        assertEquals(
            Instant.parse("2025-12-25T00:00:00Z").toEpochMilli(),
            repository.records.single().performedAt
        )
        assertFalse(viewModel.uiState.value.isSaving)
        collection.cancel()
    }

    private fun createViewModel(repository: FakeActivityRepository): AddActivityViewModel {
        val calculateCalories = CalculateActivityCaloriesUseCase()
        return AddActivityViewModel(
            activityRepository = repository,
            profileRepository = FakeProfileRepository(),
            calculateCalories = calculateCalories,
            addActivityRecord = AddActivityRecordUseCase(
                repository,
                calculateCalories,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
            ),
            ensureDefaultActivities = EnsureDefaultActivitiesUseCase(repository),
            setActivityFavorite = SetActivityFavoriteUseCase(repository)
        )
    }
}

private class FakeProfileRepository : ProfileRepository {
    override val userProfile = MutableStateFlow<UserProfile?>(UserProfile(weightKg = 60f))
    override suspend fun saveProfile(profile: UserProfile) {
        userProfile.value = profile
    }
    override suspend fun clearProfile() {
        userProfile.value = null
    }
}

private class FakeActivityRepository : ActivityRepository {
    private val activities = MutableStateFlow(
        listOf(
            PhysicalActivityType(1, "Đi bộ nhẹ", 2.8, "🚶", false, 1),
            PhysicalActivityType(2, "Chạy bộ nhẹ", 7.0, "🏃", false, 2)
        )
    )
    val records = mutableListOf<PhysicalActivityRecord>()

    override fun observeActivityTypes(): Flow<List<PhysicalActivityType>> = activities
    override fun observeRecordsByDay(epochDay: Long): Flow<List<PhysicalActivityRecord>> = emptyFlow()
    override fun observeTotalCaloriesByDay(epochDay: Long): Flow<Double> = emptyFlow()
    override fun observeTotalCaloriesBetween(startMillis: Long, endMillis: Long): Flow<Double> = emptyFlow()
    override fun observeRecordsBetween(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<PhysicalActivityRecord>> = emptyFlow()
    override suspend fun seedDefaultActivities() = Unit
    override suspend fun getActivityType(id: Long) = activities.value.firstOrNull { it.id == id }
    override suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        activities.value = activities.value.map {
            if (it.id == id) it.copy(isFavorite = isFavorite) else it
        }.sortedWith(compareByDescending<PhysicalActivityType> { it.isFavorite }.thenBy { it.displayOrder })
    }
    override suspend fun addActivityRecord(record: PhysicalActivityRecord) {
        records += record
    }
    override suspend fun deleteActivityRecord(id: Long) {
        records.removeAll { it.id == id }
    }
    override suspend fun clearActivityRecords() {
        records.clear()
    }
}
