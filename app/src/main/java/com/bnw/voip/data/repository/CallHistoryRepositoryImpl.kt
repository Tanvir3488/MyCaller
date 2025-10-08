package com.bnw.voip.data.repository

import com.bnw.voip.data.db.CallDetailsDao
import com.bnw.voip.data.entity.CallDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CallHistoryRepositoryImpl @Inject constructor(
    private val callDetailsDao: CallDetailsDao
) : CallHistoryRepository {

    override suspend fun insertCallDetails(callDetails: CallDetails) {
        callDetailsDao.insertCallDetails(callDetails)
    }

    override fun getAllCallDetails(): Flow<List<CallDetails>> {
        return callDetailsDao.getAllCallDetails()
    }
}
