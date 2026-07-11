package com.quyetbkhoa.healthtracker.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import com.quyetbkhoa.healthtracker.domain.model.Gender
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val FULL_NAME_KEY = stringPreferencesKey("profile_full_name")
        val DOB_KEY = longPreferencesKey("profile_dob")
        val GENDER_KEY = stringPreferencesKey("profile_gender")
        val WEIGHT_KEY = floatPreferencesKey("profile_weight")
        val HEIGHT_KEY = floatPreferencesKey("profile_height")
        val ACTIVITY_LEVEL_KEY = stringPreferencesKey("profile_activity_level")
        val GOAL_KEY = stringPreferencesKey("profile_goal")
        val BMR_KEY = intPreferencesKey("profile_bmr")
        val TDEE_KEY = intPreferencesKey("profile_tdee")
        val CALORIE_TARGET_KEY = intPreferencesKey("profile_calorie_target")
    }

    val userProfileFlow: Flow<UserProfile?> = dataStore.data.map { preferences ->
        val fullName = preferences[FULL_NAME_KEY]
        // If fullName is null, we can assume the profile hasn't been set up yet
        if (fullName == null) return@map null
        
        val dob = preferences[DOB_KEY] ?: 0L
        val genderStr = preferences[GENDER_KEY] ?: Gender.OTHER.name
        val weight = preferences[WEIGHT_KEY] ?: 0f
        val height = preferences[HEIGHT_KEY] ?: 0f
        val activityStr = preferences[ACTIVITY_LEVEL_KEY] ?: ActivityLevel.SEDENTARY.name
        val goalStr = preferences[GOAL_KEY] ?: Goal.MAINTAIN.name

        val gender = runCatching { Gender.valueOf(genderStr) }.getOrDefault(Gender.OTHER)
        val activityLevel = runCatching { ActivityLevel.valueOf(activityStr) }.getOrDefault(ActivityLevel.SEDENTARY)
        val goal = runCatching { Goal.valueOf(goalStr) }.getOrDefault(Goal.MAINTAIN)

        UserProfile(
            fullName = fullName,
            dateOfBirth = dob,
            gender = gender,
            weightKg = weight,
            heightCm = height,
            activityLevel = activityLevel,
            goal = goal,
            bmrCalories = preferences[BMR_KEY] ?: 0,
            tdeeCalories = preferences[TDEE_KEY] ?: 0,
            dailyCalorieTarget = preferences[CALORIE_TARGET_KEY] ?: 0
        )
    }

    suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[FULL_NAME_KEY] = profile.fullName
            preferences[DOB_KEY] = profile.dateOfBirth
            preferences[GENDER_KEY] = profile.gender.name
            preferences[WEIGHT_KEY] = profile.weightKg
            preferences[HEIGHT_KEY] = profile.heightCm
            preferences[ACTIVITY_LEVEL_KEY] = profile.activityLevel.name
            preferences[GOAL_KEY] = profile.goal.name
            preferences[BMR_KEY] = profile.bmrCalories
            preferences[TDEE_KEY] = profile.tdeeCalories
            preferences[CALORIE_TARGET_KEY] = profile.dailyCalorieTarget
        }
    }

    suspend fun clearProfile() {
        dataStore.edit { preferences ->
            preferences.remove(FULL_NAME_KEY)
            preferences.remove(DOB_KEY)
            preferences.remove(GENDER_KEY)
            preferences.remove(WEIGHT_KEY)
            preferences.remove(HEIGHT_KEY)
            preferences.remove(ACTIVITY_LEVEL_KEY)
            preferences.remove(GOAL_KEY)
            preferences.remove(BMR_KEY)
            preferences.remove(TDEE_KEY)
            preferences.remove(CALORIE_TARGET_KEY)
        }
    }
}
