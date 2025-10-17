package com.bnw.voip.data.repository

import com.bnw.voip.voip.CallState
import kotlinx.coroutines.flow.Flow

interface CallRepository {
    fun answerCall()
    fun hangupCall()
    fun getCallState(): Flow<CallState.State>
    fun makeCall(number: String)
    fun login(username: String, password: String)
    fun start()
    fun stop()
}
