package com.bnw.voip.ui.incommingcall

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bnw.voip.domain.usecase.GetContactByNumberUseCase
import com.bnw.voip.domain.usecase.call.AnswerCallUseCase
import com.bnw.voip.domain.usecase.call.GetCallStateUseCase
import com.bnw.voip.domain.usecase.call.HangupCallUseCase
import com.bnw.voip.utils.AppConstants
import com.bnw.voip.utils.CallNotificationManager
import com.bnw.voip.voip.CallStateEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.linphone.core.Call
import javax.inject.Inject

data class CallingUiState(
    val isAcceptButtonVisible: Boolean = true,
    val declineButtonText: String = "Decline"
)

@HiltViewModel
class IncomingCallViewModel @Inject constructor(
    private val answerCallUseCase: AnswerCallUseCase,
    private val hangupCallUseCase: HangupCallUseCase,
    getCallStateUseCase: GetCallStateUseCase,
    private val getContactByNumberUseCase: GetContactByNumberUseCase,
    private val callNotificationManager: CallNotificationManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var isAcceptVisibleForFirstTime = true
    val callState = getCallStateUseCase().asLiveData()

    private val _callerName = MutableStateFlow(AppConstants.UNKNOWN_CALLER)
    val callerName: StateFlow<String> = _callerName

    private val _callerNumber = MutableStateFlow("")
    val callerNumber: StateFlow<String> = _callerNumber

    private val _callType = MutableStateFlow("")
    val callType: StateFlow<String> = _callType

    private val _uiState = MutableStateFlow(CallingUiState())
    val uiState: StateFlow<CallingUiState> = _uiState

    init {
        viewModelScope.launch {
            savedStateHandle.keys().forEach { key ->
                Log.d("SavedStateHandle", "$key = ${savedStateHandle.get<String>(key)}")
            }
            val caller = savedStateHandle.get<String>(AppConstants.PHONE_NUMBER)
            val type = savedStateHandle.get<String>(AppConstants.CALL_TYPE)

            if (caller != null) {
                _callerNumber.value = caller
                val contact = getContactByNumberUseCase(caller)
                Log.e("IncomingCallViewModel", "Fetched contact: $contact for number: $caller")
                if (contact != null) {
                    _callerName.value = contact.name
                } else {
                    _callerName.value = AppConstants.UNKNOWN_CALLER
                }
            }
            if (type != null) {
                _callType.value = type
            }
        }

        viewModelScope.launch {
            combine(callState.asFlow(), callType) { state: CallStateEvent?, type: String ->
               // Log.e("IncomingCallViewModel", "Call State: $type - ${state?.state}")
                val isAcceptVisible =
                    (type == AppConstants.CALL_TYPE_INCOMING && state?.state == Call.State.IncomingReceived )
                val declineText =
                    if ((type == AppConstants.CALL_TYPE_OUTGOING || state?.state != Call.State.IncomingReceived))  "End" else "Decline"
                if (!isAcceptVisible){
                    isAcceptVisibleForFirstTime = false
                }
                CallingUiState(isAcceptVisible, declineText)

            }.collect {
                _uiState.value = it
            }
        }
    }

    fun answerCall() {
        answerCallUseCase()
        callNotificationManager.dismissNotification()
    }

    fun hangupCall() {
        hangupCallUseCase()
        callNotificationManager.dismissNotification()
    }

    fun showOngoingCallNotification() {
        viewModelScope.launch {
            val contact = getContactByNumberUseCase(callerNumber.value)
            val name = contact?.name
            Log.e("IncomingCallViewModel", "showOngoingCallNotification: $name - ${callerNumber.value}")
            callNotificationManager.showOngoingCallNotification(name, callerNumber.value)
        }
    }
}
