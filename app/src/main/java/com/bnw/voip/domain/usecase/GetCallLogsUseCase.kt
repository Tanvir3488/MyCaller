package com.bnw.voip.domain.usecase

import com.bnw.voip.data.repository.CallLogRepository
import javax.inject.Inject

class GetCallLogsUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository
) {
    suspend operator fun invoke(limit: Int, offset: Int) = callLogRepository.getCallLogs(limit, offset)
}
