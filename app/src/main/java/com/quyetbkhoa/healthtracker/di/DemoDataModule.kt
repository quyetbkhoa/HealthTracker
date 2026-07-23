package com.quyetbkhoa.healthtracker.di

import com.quyetbkhoa.healthtracker.data.repository.DemoDataRepositoryImpl
import com.quyetbkhoa.healthtracker.domain.repository.DemoDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DemoDataModule {
    @Binds
    @Singleton
    abstract fun bindDemoDataRepository(
        implementation: DemoDataRepositoryImpl
    ): DemoDataRepository
}
