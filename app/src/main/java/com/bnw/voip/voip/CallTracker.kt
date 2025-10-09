package com.bnw.voip.voip

import com.bnw.voip.data.entity.CallLogs
import com.bnw.voip.domain.usecase.AddCallLogUseCase
import com.bnw.voip.domain.usecase.call.GetCallStateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.linphone.core.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallTracker @Inject constructor(
    private val getCallStateUseCase: GetCallStateUseCase,
    private val addCallLogUseCase: AddCallLogUseCase
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentCallLog: CallLogs? = null

    fun startTracking() {
        scope.launch {
            getCallStateUseCase().collect { event ->
                when (event?.state) {
                    Call.State.OutgoingInit -> {
                        currentCallLog = CallLogs(
                            callerName = event.call?.remoteAddress?.displayName ?: "Unknown",
                            phoneNumber = event.call?.remoteAddress?.username ?: "",
                            callStartTime = System.currentTimeMillis(),
                            callEndTime = 0,
                            callDuration = 0,
                            callType = "outgoing"
                        )
                    }
                    Call.State.IncomingReceived -> {
                        currentCallLog = CallLogs(
                            callerName = event.call?.remoteAddress?.displayName ?: "Unknown",
                            phoneNumber = event.call?.remoteAddress?.username ?: "",
                            callStartTime = System.currentTimeMillis(),
                            callEndTime = 0,
                            callDuration = 0,
                            callType = "incoming"
                        )
                    }
                    Call.State.Connected -> {
                        currentCallLog = currentCallLog?.copy(callType = "answered", callStartTime = System.currentTimeMillis())
                    }
                    Call.State.End -> {
                        currentCallLog?.let {
                            val endTime = System.currentTimeMillis()
                            val duration = (endTime - it.callStartTime) / 1000
                            addCallLogUseCase(it.copy(callEndTime = endTime, callDuration = duration))
                            currentCallLog = null
                        }
                    }

                    Call.State.Released -> {

                    }
                    else -> {
                        // Do nothing
                    }
                }
            }
        }
    }
}
