package com.bnw.voip.data.repository

import com.bnw.voip.voip.CustomeSipManager
import kotlinx.coroutines.flow.Flow
import org.linphone.core.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepositoryImpl @Inject constructor(
    private val sipManager: CustomeSipManager
) : CallRepository {
    override fun answerCall() = sipManager.answerCall()
    override fun hangupCall() = sipManager.hangup()
    override fun getCallState(): Flow<com.bnw.voip.voip.CallState.State> = sipManager.callState
    override fun makeCall(number: String) = sipManager.call(number)
    override fun login(username: String, password: String) = sipManager.login(username, password)
    override fun start() = sipManager.start()
    override fun stop() = sipManager.stop()
}
