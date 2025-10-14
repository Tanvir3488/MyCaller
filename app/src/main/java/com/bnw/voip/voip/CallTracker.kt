package com.bnw.voip.voip

import android.os.SystemClock
import android.util.Log
import com.bnw.voip.data.entity.CallLogs
import com.bnw.voip.domain.usecase.AddCallLogUseCase
import com.bnw.voip.domain.usecase.call.GetCallStateUseCase
import com.bnw.voip.utils.AppConstants.CALL_TYPE_ANSWERED
import com.bnw.voip.utils.AppConstants.CALL_TYPE_INCOMING
import com.bnw.voip.utils.AppConstants.CALL_TYPE_MISSED
import com.bnw.voip.utils.AppConstants.CALL_TYPE_OUTGOING
import com.bnw.voip.utils.AppConstants.UNKNOWN_CALLER
import com.bnw.voip.utils.CallNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.linphone.core.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallTracker @Inject constructor(
    private val getCallStateUseCase: GetCallStateUseCase,
    private val addCallLogUseCase: AddCallLogUseCase,
    private val callNotificationManager: CallNotificationManager
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentCallLog: CallLogs? = null
    private val _callConnectedTime = MutableStateFlow<Long?>(null)
    val callConnectedTime: StateFlow<Long?> = _callConnectedTime.asStateFlow()

    private val _callConnectedWallTime = MutableStateFlow<Long?>(null)
    val callConnectedWallTime: StateFlow<Long?> = _callConnectedWallTime.asStateFlow()
    var isCallConnected : Boolean = false
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
                        if (_callConnectedTime.value == null) {
                            _callConnectedTime.value = SystemClock.elapsedRealtime()
                            _callConnectedWallTime.value = System.currentTimeMillis()
                        }
                        currentCallLog = currentCallLog?.copy(callType = CALL_TYPE_ANSWERED, callStartTime = System.currentTimeMillis())
                    }
                    Call.State.End -> {
                        _callConnectedTime.value = null
                        _callConnectedWallTime.value = null
                        callNotificationManager.dismissNotification()
                        currentCallLog?.let {
                            Log.e("isCallConnected",isCallConnected.toString())
                            val endTime = System.currentTimeMillis()
                            val duration = if (isCallConnected) (endTime - it.callStartTime) / 1000 else 0
                            val callType = if (it.callType == CALL_TYPE_INCOMING && !isCallConnected) {
                                CALL_TYPE_MISSED
                            } else if (isCallConnected) {
                                CALL_TYPE_ANSWERED
                            } else {
                                it.callType
                            }
                            Log.e("isCallConnected",callType+" "+it.callType)
                            if (callType == CALL_TYPE_MISSED) {
                                callNotificationManager.showMissedCallNotification(it.callerName, it.phoneNumber)
                            }

                            addCallLogUseCase(it.copy(callEndTime = endTime, callDuration = duration, callType = callType))
                            currentCallLog = null
                        }
                    }

                    Call.State.Released -> {
                        _callConnectedTime.value = null
                        _callConnectedWallTime.value = null
                        callNotificationManager.dismissNotification()
                    }
                    else -> {
                        // Do nothing
                    }
                }
            }
        }
    }
}
