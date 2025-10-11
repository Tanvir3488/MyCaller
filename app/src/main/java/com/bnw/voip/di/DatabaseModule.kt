package com.bnw.voip.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE contacts ADD COLUMN deviceContactId INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE contacts ADD COLUMN photoUri TEXT")
            database.execSQL("ALTER TABLE contacts ADD COLUMN lastUpdatedTimestamp INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    @Singleton
    fun provideVoipDatabase(@ApplicationContext context: Context): VoipDatabase {
        return Room.databaseBuilder(
            context,
            VoipDatabase::class.java,
            "voip_database"
        ).addMigrations(MIGRATION_1_2).build()
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
