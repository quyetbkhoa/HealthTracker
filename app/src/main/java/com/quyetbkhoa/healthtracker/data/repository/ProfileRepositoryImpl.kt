package com.quyetbkhoa.healthtracker.data.repository

import com.quyetbkhoa.healthtracker.data.datastore.ProfileDataStore
import com.quyetbkhoa.healthtracker.domain.model.UserProfile
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileDataStore: ProfileDataStore
) : ProfileRepository {

    override val userProfile: Flow<UserProfile?>
        get() = profileDataStore.userProfileFlow

    override suspend fun saveProfile(profile: UserProfile) {
        profileDataStore.saveProfile(profile)
    }
}
