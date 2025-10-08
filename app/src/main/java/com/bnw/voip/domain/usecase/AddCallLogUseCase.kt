package com.bnw.voip.domain.usecase

import com.bnw.voip.data.entity.CallDetails
import com.bnw.voip.data.repository.CallHistoryRepository
import javax.inject.Inject

class AddCallLogUseCase @Inject constructor(
    private val callHistoryRepository: CallHistoryRepository
) {
    suspend operator fun invoke(callDetails: CallDetails) {
        callHistoryRepository.insertCallDetails(callDetails)
    }
}
