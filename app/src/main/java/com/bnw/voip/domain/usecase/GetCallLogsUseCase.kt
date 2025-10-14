package com.bnw.voip.domain.usecase

import com.bnw.voip.data.entity.CallLogs
import com.bnw.voip.data.repository.CallLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCallLogsUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository
) {
    operator fun invoke(): Flow<List<CallLogs>> = callLogRepository.getCallLogs()
}
