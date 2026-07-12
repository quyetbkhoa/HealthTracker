package com.quyetbkhoa.healthtracker.data.local.activity

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quyetbkhoa.healthtracker.data.local.HealthTrackerDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityDaoTest {
    private lateinit var database: HealthTrackerDatabase
    private lateinit var dao: ActivityDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HealthTrackerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.activityDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun seedIsIdempotentAndFavoritesSortFirst() = runBlocking {
        dao.seedDefaults()
        dao.seedDefaults()
        assertEquals(18, dao.observeActivityTypes().first().size)

        dao.updateFavorite(10, true)
        assertEquals(10L, dao.observeActivityTypes().first().first().id)
    }

    @Test
    fun storesRecordsAndTotalsOnlyRequestedDay() = runBlocking {
        dao.seedDefaults()
        dao.insertActivityRecord(record(performedAt = 1_000, calories = 120.5))
        dao.insertActivityRecord(record(performedAt = 2_000, calories = 79.5))
        dao.insertActivityRecord(record(performedAt = 20_000, calories = 300.0))

        assertEquals(2, dao.observeRecordsBetween(0, 10_000).first().size)
        assertEquals(200.0, dao.observeTotalCaloriesBetween(0, 10_000).first(), 0.0001)
    }

    private fun record(performedAt: Long, calories: Double) = ActivityRecordEntity(
        activityTypeId = 1,
        durationMinutes = 30,
        metAtCreation = 2.8,
        weightKgAtCreation = 60.0,
        caloriesBurned = calories,
        performedAt = performedAt,
        createdAt = performedAt
    )
}
