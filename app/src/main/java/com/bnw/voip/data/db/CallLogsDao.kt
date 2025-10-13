package com.bnw.voip.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bnw.voip.data.entity.CallLogs

@Dao
interface CallLogsDao {
    @Insert
    suspend fun insert(callLogs: CallLogs)

    @Query("SELECT * FROM call_logs ORDER BY callStartTime DESC LIMIT :limit OFFSET :offset")
    suspend fun getCallLogs(limit: Int, offset: Int): List<CallLogs>
}
