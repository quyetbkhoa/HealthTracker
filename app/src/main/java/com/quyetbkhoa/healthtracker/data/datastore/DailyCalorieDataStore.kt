package com.quyetbkhoa.healthtracker.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.quyetbkhoa.healthtracker.domain.model.DailyCalorieSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DailyCalorieDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val EPOCH_DAY = longPreferencesKey("daily_calorie_epoch_day")
        val CONSUMED = intPreferencesKey("daily_calorie_consumed")
        val EXERCISE = intPreferencesKey("daily_calorie_exercise")
    }

    fun observeSummary(epochDay: Long): Flow<DailyCalorieSummary> = dataStore.data.map { preferences ->
        if (preferences[EPOCH_DAY] != epochDay) {
            DailyCalorieSummary(epochDay = epochDay)
        } else {
            DailyCalorieSummary(
                epochDay = epochDay,
                consumedCalories = preferences[CONSUMED] ?: 0,
                exerciseCalories = preferences[EXERCISE] ?: 0
            )
        }
    }

    suspend fun updateConsumedCalories(epochDay: Long, calories: Int) {
        updateDay(epochDay) { it[CONSUMED] = calories.coerceAtLeast(0) }
    }

    suspend fun updateExerciseCalories(epochDay: Long, calories: Int) {
        updateDay(epochDay) { it[EXERCISE] = calories.coerceAtLeast(0) }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(EPOCH_DAY)
            preferences.remove(CONSUMED)
            preferences.remove(EXERCISE)
        }
    }

    private suspend fun updateDay(epochDay: Long, update: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit { preferences ->
            if (preferences[EPOCH_DAY] != epochDay) {
                preferences[EPOCH_DAY] = epochDay
                preferences[CONSUMED] = 0
                preferences[EXERCISE] = 0
            }
            update(preferences)
        }
    }
}
