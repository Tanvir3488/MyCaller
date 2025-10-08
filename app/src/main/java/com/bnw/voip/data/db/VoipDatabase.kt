package com.bnw.voip.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bnw.voip.data.db.converter.StringListConverter
import com.bnw.voip.data.entity.CallDetails
import com.bnw.voip.data.entity.Contact

@Database(entities = [CallDetails::class, Contact::class], version = 3, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class VoipDatabase : RoomDatabase() {

    abstract fun callDetailsDao(): CallDetailsDao
    abstract fun contactDao(): ContactDao
}
