package com.bnw.voip.data.repository

import com.bnw.voip.data.entity.CallLogs

interface CallLogRepository {
    suspend fun getCallLogs(limit: Int, offset: Int): List<CallLogs>
    suspend fun insertCallLog(callLogs: CallLogs)
}
