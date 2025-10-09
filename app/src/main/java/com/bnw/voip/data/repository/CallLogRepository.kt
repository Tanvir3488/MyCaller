package com.bnw.voip.data.repository

import com.bnw.voip.data.entity.CallLogs
import kotlinx.coroutines.flow.Flow

interface CallLogRepository {
    fun getCallLogs(): Flow<List<CallLogs>>
    suspend fun insertCallLog(callLogs: CallLogs)
}
