package com.bnw.voip.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bnw.voip.data.entity.CallDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDetailsDao {

    @Insert
    suspend fun insertCallDetails(callDetails: CallDetails): Long

    @Query("SELECT * FROM call_details ORDER BY callTimestamp DESC")
    fun getAllCallDetails(): Flow<List<CallDetails>>
}
