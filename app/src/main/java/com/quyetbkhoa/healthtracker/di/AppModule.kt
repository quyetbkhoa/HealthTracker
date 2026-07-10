package com.quyetbkhoa.healthtracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.quyetbkhoa.healthtracker.data.datastore.SettingsDataStore
import com.quyetbkhoa.healthtracker.data.repository.SettingsRepositoryImpl
import com.quyetbkhoa.healthtracker.domain.repository.SettingsRepository
import com.quyetbkhoa.healthtracker.data.datastore.ProfileDataStore
import com.quyetbkhoa.healthtracker.data.repository.ProfileRepositoryImpl
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings") }
        )
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(dataStore: DataStore<Preferences>): SettingsDataStore {
        return SettingsDataStore(dataStore)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(settingsDataStore: SettingsDataStore): SettingsRepository {
        return SettingsRepositoryImpl(settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideProfileDataStore(dataStore: DataStore<Preferences>): ProfileDataStore {
        return ProfileDataStore(dataStore)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(profileDataStore: ProfileDataStore): ProfileRepository {
        return ProfileRepositoryImpl(profileDataStore)
    }
}
