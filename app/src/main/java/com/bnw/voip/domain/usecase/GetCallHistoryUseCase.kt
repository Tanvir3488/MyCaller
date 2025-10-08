package com.bnw.voip.domain.usecase

import com.bnw.voip.data.entity.CallDetails
import com.bnw.voip.data.repository.CallHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCallHistoryUseCase @Inject constructor(
    private val callHistoryRepository: CallHistoryRepository
) {
    operator fun invoke(): Flow<List<CallDetails>> {
        return callHistoryRepository.getAllCallDetails()
    }
}
