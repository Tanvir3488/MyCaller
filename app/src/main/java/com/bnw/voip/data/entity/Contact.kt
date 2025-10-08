package com.bnw.voip.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bnw.voip.data.db.converter.StringListConverter

@Entity(tableName = "contacts")
@TypeConverters(StringListConverter::class)
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumbers: List<String>
)
