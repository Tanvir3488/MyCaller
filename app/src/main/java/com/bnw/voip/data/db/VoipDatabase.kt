package com.bnw.voip.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bnw.voip.data.db.converter.StringListConverter
import com.bnw.voip.data.entity.CallLogs
import com.bnw.voip.data.entity.Contact

@Database(entities = [Contact::class, CallLogs::class], version = 2, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class VoipDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun callLogsDao(): CallLogsDao
}
