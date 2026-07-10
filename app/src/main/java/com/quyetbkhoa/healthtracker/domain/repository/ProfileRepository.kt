package com.quyetbkhoa.healthtracker.domain.repository

import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val userProfile: Flow<UserProfile?>
    suspend fun saveProfile(profile: UserProfile)
}
