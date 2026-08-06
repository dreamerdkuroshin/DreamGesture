package com.gestureshare.core.di

import com.gestureshare.core.data.repository.GestureRepositoryImpl
import com.gestureshare.core.data.repository.NearbyDeviceRepositoryImpl
import com.gestureshare.core.data.repository.ScreenshotRepositoryImpl
import com.gestureshare.core.domain.repository.DeviceRepository
import com.gestureshare.core.domain.repository.GestureRepository
import com.gestureshare.core.domain.repository.ScreenshotRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindScreenshotRepository(
        impl: ScreenshotRepositoryImpl
    ): ScreenshotRepository

    @Binds
    @Singleton
    abstract fun bindGestureRepository(
        impl: GestureRepositoryImpl
    ): GestureRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(
        impl: NearbyDeviceRepositoryImpl
    ): DeviceRepository
}
