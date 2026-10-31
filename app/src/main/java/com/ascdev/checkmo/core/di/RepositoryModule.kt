package com.ascdev.checkmo.core.di

import com.ascdev.checkmo.data.auth.FirebaseAuthRepository
import com.ascdev.checkmo.data.timeentry.RoomTimeEntryRepository
import com.ascdev.checkmo.data.workplace.FirestoreWorkplaceRepository
import com.ascdev.checkmo.domain.repository.AuthRepository
import com.ascdev.checkmo.domain.repository.TimeEntryRepository
import com.ascdev.checkmo.domain.repository.WorkplaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    abstract fun bindTimeEntryRepository(impl: RoomTimeEntryRepository): TimeEntryRepository

    @Binds
    abstract fun bindWorkplaceRepository(impl: FirestoreWorkplaceRepository): WorkplaceRepository
}
