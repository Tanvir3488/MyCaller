package com.bnw.voip.voip

import com.bnw.voip.data.entity.CallLogs
import com.bnw.voip.domain.usecase.AddCallLogUseCase
import com.bnw.voip.domain.usecase.call.GetCallStateUseCase
import com.bnw.voip.utils.AppConstants.CALL_TYPE_ANSWERED
import com.bnw.voip.utils.AppConstants.CALL_TYPE_INCOMING
import com.bnw.voip.utils.AppConstants.CALL_TYPE_MISSED
import com.bnw.voip.utils.AppConstants.CALL_TYPE_OUTGOING
import com.bnw.voip.utils.AppConstants.UNKNOWN_CALLER
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
    var isCallConnected: Boolean = false
    fun startTracking() {
        scope.launch {
            getCallStateUseCase().collect { event ->
                when (event?.state) {
                    Call.State.OutgoingInit -> {
                        currentCallLog = CallLogs(
                            callerName = event.call?.remoteAddress?.displayName ?: UNKNOWN_CALLER,
                            phoneNumber = event.call?.remoteAddress?.username ?: "",
                            callStartTime = System.currentTimeMillis(),
                            callEndTime = 0,
                            callDuration = 0,
                            callType = CALL_TYPE_OUTGOING
                        )
                    }
                    Call.State.IncomingReceived -> {
                        currentCallLog = CallLogs(
                            callerName = event.call?.remoteAddress?.displayName ?: UNKNOWN_CALLER,
                            phoneNumber = event.call?.remoteAddress?.username ?: "",
                            callStartTime = System.currentTimeMillis(),
                            callEndTime = 0,
                            callDuration = 0,
                            callType = CALL_TYPE_INCOMING
                        )
                    }
                    Call.State.Connected -> {
                        isCallConnected = true
                        currentCallLog = currentCallLog?.copy(callType = CALL_TYPE_ANSWERED, callStartTime = System.currentTimeMillis())
                    }
                    Call.State.End -> {

                        currentCallLog?.let {
                            val endTime = System.currentTimeMillis()
                            var duration = (endTime - it.callStartTime) / 1000
                            if (!isCallConnected){
                                duration = 0
                            }
                            addCallLogUseCase(it.copy(callEndTime = endTime, callDuration = duration, callType = if (isCallConnected) CALL_TYPE_ANSWERED else CALL_TYPE_MISSED))
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
