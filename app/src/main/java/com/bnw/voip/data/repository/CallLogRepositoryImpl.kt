package com.bnw.voip.data.repository

import com.bnw.voip.data.db.CallLogsDao
import com.bnw.voip.data.entity.CallLogs
import javax.inject.Inject

class CallLogRepositoryImpl @Inject constructor(
    private val callLogsDao: CallLogsDao
) : CallLogRepository {
    override suspend fun getCallLogs(limit: Int, offset: Int): List<CallLogs> = callLogsDao.getCallLogs(limit, offset)

    override suspend fun insertCallLog(callLogs: CallLogs) {
        callLogsDao.insert(callLogs)
    }
}
