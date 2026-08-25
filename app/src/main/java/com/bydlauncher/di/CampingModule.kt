package com.bydlauncher.di

import android.content.Context
import com.bydlauncher.camping.storage.CampingPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CampingModule {

    @Provides
    @Singleton
    fun provideCampingPrefs(@ApplicationContext context: Context): CampingPrefs =
        CampingPrefs(context)
}
