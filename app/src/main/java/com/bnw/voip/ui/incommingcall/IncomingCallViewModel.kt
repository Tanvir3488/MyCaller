package com.bnw.voip.ui.incommingcall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bnw.voip.domain.usecase.call.AnswerCallUseCase
import com.bnw.voip.domain.usecase.call.GetCallStateUseCase
import com.bnw.voip.domain.usecase.call.HangupCallUseCase
import com.bnw.voip.utils.CallNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IncomingCallViewModel @Inject constructor(
    private val answerCallUseCase: AnswerCallUseCase,
    private val hangupCallUseCase: HangupCallUseCase,
    getCallStateUseCase: GetCallStateUseCase,
    private val callNotificationManager: CallNotificationManager
) : ViewModel() {

    val callState = getCallStateUseCase().asLiveData()

    fun answerCall() {
        answerCallUseCase()
        callNotificationManager.dismissNotification()
    }

    fun hangupCall() {
        hangupCallUseCase()
        callNotificationManager.dismissNotification()
    }
}
