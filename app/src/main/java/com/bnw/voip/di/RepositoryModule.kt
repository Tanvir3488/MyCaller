package com.bnw.voip.di

import com.bnw.voip.data.repository.CallHistoryRepository
import com.bnw.voip.data.repository.CallHistoryRepositoryImpl
import com.bnw.voip.data.repository.ContactRepository
import com.bnw.voip.data.repository.ContactRepositoryImpl
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
    abstract fun bindCallHistoryRepository(
        callHistoryRepositoryImpl: CallHistoryRepositoryImpl
    ): CallHistoryRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        contactRepositoryImpl: ContactRepositoryImpl
    ): ContactRepository
}
