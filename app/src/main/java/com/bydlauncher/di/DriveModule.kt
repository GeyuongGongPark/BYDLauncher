package com.bydlauncher.di

import android.content.Context
import androidx.room.Room
import com.bydlauncher.data.drive.DriveRepositoryImpl
import com.bydlauncher.data.drive.db.DriveDatabase
import com.bydlauncher.domain.drive.DriveRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DriveDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DriveDatabase =
        Room.databaseBuilder(context, DriveDatabase::class.java, "drive_db")
            .fallbackToDestructiveMigration()
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DriveRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDriveRepository(impl: DriveRepositoryImpl): DriveRepository
}
