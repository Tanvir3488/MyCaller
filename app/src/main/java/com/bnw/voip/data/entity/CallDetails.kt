package com.bnw.voip.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_details")
data class CallDetails(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val callerName: String,
    val phoneNumber: String,
    val callDuration: Long,
    val callTimestamp: Long,
    val callType: String // e.g., "incoming", "outgoing", "missed"
)
