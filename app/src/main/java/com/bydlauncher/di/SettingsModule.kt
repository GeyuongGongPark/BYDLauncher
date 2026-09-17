package com.bydlauncher.di

import com.bydlauncher.data.settings.AppSettingsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Provides
    @Singleton
    fun provideAppSettingsRepository(impl: AppSettingsRepositoryImpl): AppSettingsRepositoryImpl = impl
}
