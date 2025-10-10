package com.bnw.voip.ui.incommingcall

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
    private val savedStateHandle: SavedStateHandle,
    private val callNotificationManager: CallNotificationManager
) : ViewModel() {

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
            val caller = savedStateHandle.get<String>(AppConstants.CALLER_NAME)
            val type = savedStateHandle.get<String>(AppConstants.CALL_TYPE)
            if (caller != null) {
                _callerNumber.value = caller
                val contact = getContactByNumberUseCase(caller)
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
                val isAcceptVisible = type == AppConstants.CALL_TYPE_INCOMING && state?.state != Call.State.Connected && state?.state != Call.State.StreamsRunning
                val declineText = if (type == AppConstants.CALL_TYPE_OUTGOING || state?.state == Call.State.Connected || state?.state == Call.State.StreamsRunning) "End" else "Decline"
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
}