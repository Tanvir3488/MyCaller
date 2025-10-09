package com.bnw.voip.domain.usecase

import com.bnw.voip.data.entity.CallLogs
import com.bnw.voip.data.repository.CallLogRepository
import javax.inject.Inject

class AddCallLogUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository
) {
    suspend operator fun invoke(callLogs: CallLogs) {
        callLogRepository.insertCallLog(callLogs)
    }
}
