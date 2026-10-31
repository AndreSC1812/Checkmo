package com.ascdev.checkmo.core.di

import android.content.Context
import com.ascdev.checkmo.data.location.FusedLocationProvider
import com.ascdev.checkmo.domain.location.LocationProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    abstract fun bindLocationProvider(impl: FusedLocationProvider): LocationProvider

    companion object {
        @Provides
        fun provideFusedLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
    }
}
