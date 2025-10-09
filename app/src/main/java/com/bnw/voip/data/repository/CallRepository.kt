package com.bnw.voip.data.repository

import kotlinx.coroutines.flow.Flow
import org.linphone.core.Call

interface CallRepository {
    fun answerCall()
    fun hangupCall()
    fun getCallState(): Flow<com.bnw.voip.voip.CallStateEvent?>
    fun makeCall(number: String)
    fun login()
    fun start()
    fun stop()
}
