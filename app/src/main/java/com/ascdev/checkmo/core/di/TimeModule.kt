package com.ascdev.checkmo.core.di

import com.ascdev.checkmo.core.time.SystemTicker
import com.ascdev.checkmo.core.time.Ticker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {

    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    fun provideTicker(clock: Clock): Ticker = SystemTicker(clock)
}
