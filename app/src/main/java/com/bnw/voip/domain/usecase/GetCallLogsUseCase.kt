package com.bnw.voip.domain.usecase

import com.bnw.voip.data.repository.CallLogRepository
import javax.inject.Inject

class GetCallLogsUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository
) {
    operator fun invoke() = callLogRepository.getCallLogs()
}
