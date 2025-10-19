package com.bnw.voip.di


import com.bnw.voip.data.repository.CallLogRepository
import com.bnw.voip.data.repository.CallLogRepositoryImpl
import com.bnw.voip.data.repository.CallRepository
import com.bnw.voip.data.repository.CallRepositoryImpl
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
    abstract fun bindCallRepository(impl: CallRepositoryImpl): CallRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        contactRepositoryImpl: ContactRepositoryImpl
    ): ContactRepository

    @Binds
    @Singleton
    abstract fun bindCallLogRepository(impl: CallLogRepositoryImpl): CallLogRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: com.bnw.voip.data.repository.ProfileRepositoryImpl): com.bnw.voip.data.repository.ProfileRepository
}
