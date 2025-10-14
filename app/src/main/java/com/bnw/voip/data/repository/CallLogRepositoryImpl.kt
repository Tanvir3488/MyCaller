package com.bnw.voip.data.repository

import com.bnw.voip.data.db.CallLogsDao
import com.bnw.voip.data.entity.CallLogs
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CallLogRepositoryImpl @Inject constructor(
    private val callLogsDao: CallLogsDao
) : CallLogRepository {
    override fun getCallLogs(): Flow<List<CallLogs>> = callLogsDao.getCallLogs()

    override suspend fun insertCallLog(callLogs: CallLogs) {
        callLogsDao.insert(callLogs)
    }
}
