package com.bydlauncher.di

import com.bydlauncher.data.navi.NaviRepositoryImpl
import com.bydlauncher.domain.navi.NaviRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NaviModule {

    @Binds
    @Singleton
    abstract fun bindNaviRepository(impl: NaviRepositoryImpl): NaviRepository
}
