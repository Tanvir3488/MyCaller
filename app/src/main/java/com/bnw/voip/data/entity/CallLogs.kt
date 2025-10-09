package com.bnw.voip.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLogs(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val callerName: String,
    val phoneNumber: String,
    val callDuration: Long, // in seconds
    val callStartTime: Long,
    val callEndTime: Long,
    val callType: String ,
    val isAnswered: Boolean = false
)
