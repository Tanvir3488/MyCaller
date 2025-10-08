package com.bnw.voip.data.repository

import com.bnw.voip.data.entity.CallDetails
import kotlinx.coroutines.flow.Flow

interface CallHistoryRepository {

    suspend fun insertCallDetails(callDetails: CallDetails)

    fun getAllCallDetails(): Flow<List<CallDetails>>
}
