package com.bydlauncher.di

import com.bydlauncher.data.apps.AppRepositoryImpl
import com.bydlauncher.data.kbo.KboRepositoryImpl
import com.bydlauncher.domain.apps.AppRepository
import com.bydlauncher.domain.kbo.KboRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAppRepository(impl: AppRepositoryImpl): AppRepository

    @Binds
    @Singleton
    abstract fun bindKboRepository(impl: KboRepositoryImpl): KboRepository
}
