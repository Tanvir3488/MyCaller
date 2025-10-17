package com.bnw.voip.voip

import org.linphone.core.Call

sealed class CallState {
    object Idle : CallState()
    data class Incoming(val call: Call) : CallState()
    data class Outgoing(val call: Call) : CallState()
    data class Connected(val call: Call) : CallState()
    data class Released(val call: Call) : CallState()
    data class Error(val call: Call, val message: String) : CallState()

    sealed class RegistrationState {
        object Idle : RegistrationState()
        object Progress : RegistrationState()
        object Ok : RegistrationState()
        data class Failed(val message: String) : RegistrationState()
    }

    data class State(val callState: CallState, val registrationState: RegistrationState)
}
