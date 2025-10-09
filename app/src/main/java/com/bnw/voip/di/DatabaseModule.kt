package com.bnw.voip.di

import android.content.Context
import androidx.room.Room
import com.bnw.voip.data.db.CallLogsDao
import com.bnw.voip.data.db.ContactDao
import com.bnw.voip.data.db.VoipDatabase
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
    fun provideVoipDatabase(@ApplicationContext context: Context): VoipDatabase {
        return Room.databaseBuilder(
            context,
            VoipDatabase::class.java,
            "voip_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideCallLogsDao(database: VoipDatabase): CallLogsDao {
        return database.callLogsDao()
    }

    @Provides
    fun provideContactDao(voipDatabase: VoipDatabase): ContactDao {
        return voipDatabase.contactDao()
    }
}
