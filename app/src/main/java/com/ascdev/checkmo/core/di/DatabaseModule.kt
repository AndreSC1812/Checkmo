package com.ascdev.checkmo.core.di

import android.content.Context
import androidx.room.Room
import com.ascdev.checkmo.data.local.CheckmoDatabase
import com.ascdev.checkmo.data.local.TimeEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CheckmoDatabase =
        Room.databaseBuilder(context, CheckmoDatabase::class.java, "checkmo.db").build()

    @Provides
    fun provideTimeEntryDao(database: CheckmoDatabase): TimeEntryDao = database.timeEntryDao()
}
