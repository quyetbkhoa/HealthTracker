package com.quyetbkhoa.healthtracker.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quyetbkhoa.healthtracker.domain.model.FontScale
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        val THEME_TYPE_KEY = stringPreferencesKey("theme_type")
        val FONT_SIZE_KEY = stringPreferencesKey("font_size")
        val REMINDERS_ENABLED_KEY = booleanPreferencesKey("reminders_enabled")
        val EXACT_ALARM_ACCESS_REQUESTED_KEY = booleanPreferencesKey("exact_alarm_access_requested")
        val BREAKFAST_TIME_KEY = intPreferencesKey("breakfast_reminder_minutes")
        val LUNCH_TIME_KEY = intPreferencesKey("lunch_reminder_minutes")
        val DINNER_TIME_KEY = intPreferencesKey("dinner_reminder_minutes")
        val ACTIVITY_TIME_KEY = intPreferencesKey("activity_reminder_minutes")
        const val MINUTES_PER_HOUR = 60
        const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
    }

    val themeModeFlow: Flow<ThemeMode> = dataStore.data
        .map { preferences ->
            val themeString = preferences[THEME_TYPE_KEY] ?: ThemeMode.SYSTEM.name
            runCatching { ThemeMode.valueOf(themeString) }.getOrDefault(ThemeMode.SYSTEM)
        }

    val fontScaleFlow: Flow<FontScale> = dataStore.data
        .map { preferences ->
            val fontScale = preferences[FONT_SIZE_KEY] ?: FontScale.MEDIUM.name
            runCatching { FontScale.valueOf(fontScale) }.getOrDefault(FontScale.MEDIUM)
        }

    val reminderSettingsFlow: Flow<ReminderSettings> = dataStore.data
        .map { preferences ->
            ReminderSettings(
                isEnabled = preferences[REMINDERS_ENABLED_KEY] ?: true,
                breakfast = preferences.toReminderTime(BREAKFAST_TIME_KEY, 9),
                lunch = preferences.toReminderTime(LUNCH_TIME_KEY, 13),
                dinner = preferences.toReminderTime(DINNER_TIME_KEY, 19),
                activity = preferences.toReminderTime(ACTIVITY_TIME_KEY, 21)
            )
        }

    val exactAlarmAccessRequestedFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[EXACT_ALARM_ACCESS_REQUESTED_KEY] ?: false }

    suspend fun saveThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_TYPE_KEY] = themeMode.name
        }
    }

    suspend fun saveFontScale(fontScale: FontScale) {
        dataStore.edit { preferences ->
            preferences[FONT_SIZE_KEY] = fontScale.name
        }
    }

    suspend fun saveRemindersEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[REMINDERS_ENABLED_KEY] = isEnabled
        }
    }

    suspend fun saveReminderTime(type: ReminderType, time: ReminderTime) {
        dataStore.edit { preferences ->
            preferences[keyFor(type)] = time.hour * MINUTES_PER_HOUR + time.minute
        }
    }

    suspend fun markExactAlarmAccessRequested() {
        dataStore.edit { preferences ->
            preferences[EXACT_ALARM_ACCESS_REQUESTED_KEY] = true
        }
    }

    private fun Preferences.toReminderTime(
        key: Preferences.Key<Int>,
        defaultHour: Int
    ): ReminderTime {
        val totalMinutes = (this[key] ?: defaultHour * MINUTES_PER_HOUR)
            .coerceIn(0, MINUTES_PER_DAY - 1)
        return ReminderTime(
            hour = totalMinutes / MINUTES_PER_HOUR,
            minute = totalMinutes % MINUTES_PER_HOUR
        )
    }

    private fun keyFor(type: ReminderType): Preferences.Key<Int> = when (type) {
        ReminderType.BREAKFAST -> BREAKFAST_TIME_KEY
        ReminderType.LUNCH -> LUNCH_TIME_KEY
        ReminderType.DINNER -> DINNER_TIME_KEY
        ReminderType.ACTIVITY -> ACTIVITY_TIME_KEY
    }

}
