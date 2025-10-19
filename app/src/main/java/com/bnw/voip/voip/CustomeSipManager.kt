package com.bnw.voip.voip

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bnw.voip.data.datastore.UserManager
import com.bnw.voip.domain.usecase.AddCallLogUseCase
import com.bnw.voip.ui.incommingcall.CallingActivity
import com.bnw.voip.utils.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.linphone.core.Account
import org.linphone.core.AudioDevice
import org.linphone.core.AVPFMode
import org.linphone.core.Call
import org.linphone.core.CallStats
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.GlobalState
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CustomeSipManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val core: Core,
    private val addCallLogUseCase: AddCallLogUseCase,
    private val userManager: UserManager
) {
    private val handler = Handler(Looper.getMainLooper())
    private var isStarted = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _callState = MutableStateFlow(CallState.State(CallState.Idle, CallState.RegistrationState.Idle))
    val callState: StateFlow<CallState.State> = _callState.asStateFlow()

    private val iterateRunnable = object : Runnable {
        override fun run() {
            if (isStarted) {
                core.iterate()
                handler.postDelayed(this, 20) // Iterate every 20ms
            }
        }
    }

    init {
        Log.d(AppConstants.TAG_SIP_MANAGER, "Initializing SipManager")
        // Add core listener
        core.addListener(object : CoreListenerStub() {
            override fun onAccountRegistrationStateChanged(
                core: Core,
                account: Account,
                state: RegistrationState,
                message: String
            ) {
                Log.d(AppConstants.TAG_SIP_MANAGER, "Registration state: $state, message: $message")
                val registrationState = when (state) {
                    RegistrationState.Ok -> CallState.RegistrationState.Ok
                    RegistrationState.Failed -> CallState.RegistrationState.Failed(message)
                    RegistrationState.Progress -> CallState.RegistrationState.Progress
                    else -> _callState.value.registrationState
                }
                _callState.value = _callState.value.copy(registrationState = registrationState)
            }

            override fun onCallStateChanged(
                core: Core,
                call: Call,
                state: Call.State,
                message: String
            ) {
                Log.d(AppConstants.TAG_SIP_MANAGER, "Call state changed: $state, message: $message")
                val callState = when (state) {
                    Call.State.IncomingReceived -> CallState.Incoming(call)
                    Call.State.OutgoingInit, Call.State.OutgoingProgress, Call.State.OutgoingRinging -> CallState.Outgoing(call)
                    Call.State.Connected, Call.State.StreamsRunning -> CallState.Connected(call)
                    Call.State.Released -> CallState.Released(call)
                    Call.State.Error -> CallState.Error(call, message)
                    else -> _callState.value.callState
                }
                _callState.value = _callState.value.copy(callState = callState)
            }

            override fun onGlobalStateChanged(
                core: Core,
                state: GlobalState,
                message: String
            ) {
                Log.d(AppConstants.TAG_SIP_MANAGER, "Global state changed: $state, message: $message")
            }

            override fun onNetworkReachable(core: Core, reachable: Boolean) {
                Log.d(AppConstants.TAG_SIP_MANAGER, "Network reachable: $reachable")
                if (!reachable) {
                    Log.w(AppConstants.TAG_SIP_MANAGER, "Network is not reachable!")
                }
            }

            override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {
                Log.d(AppConstants.TAG_SIP_MANAGER, "Audio device changed: ${audioDevice.deviceName}")
            }

            override fun onCallStatsUpdated(core: Core, call: Call, stats: CallStats) {
                // Uncomment for detailed call statistics
                // Log.v(AppConstants.TAG_SIP_MANAGER, "Call stats updated")
            }
        })

        Log.d(AppConstants.TAG_SIP_MANAGER, "SipManager initialized successfully")
    }

    /**
     * Start the Linphone core
     */
    fun start() {
        if (isStarted) {
            Log.w(AppConstants.TAG_SIP_MANAGER, "SipManager already started")
            return
        }

        Log.d(AppConstants.TAG_SIP_MANAGER, "Starting SipManager")
        core.start()
        isStarted = true
        handler.post(iterateRunnable)
        Log.i(AppConstants.TAG_SIP_MANAGER, "SipManager started successfully")
    }

    /**
     * Stop the Linphone core
     */
    fun stop() {
        if (!isStarted) {
            Log.w(AppConstants.TAG_SIP_MANAGER, "SipManager already stopped")
            return
        }

        Log.d(AppConstants.TAG_SIP_MANAGER, "Stopping SipManager")
        isStarted = false
        handler.removeCallbacks(iterateRunnable)

        // Hangup all calls
        if (core.callsNb > 0) {
            core.terminateAllCalls()
        }

        core.stop()
        Log.i(AppConstants.TAG_SIP_MANAGER, "SipManager stopped successfully")
    }

    /**
     * Login/Register with SIP server
     */
    fun login(username: String, password: String) {
        try {
            Log.d(AppConstants.TAG_SIP_MANAGER, "Attempting login - Username: $username, Domain: ${Constants.DOMAIN}")

            // Create authentication info
            val authInfo = Factory.instance().createAuthInfo(
                username,    // username
                username,    // userid
                password,    // password
                null,                  // ha1
                null,                  // realm (null = any realm)
                Constants.DOMAIN       // domain
            )
            core.addAuthInfo(authInfo)
            Log.d(AppConstants.TAG_SIP_MANAGER, "AuthInfo added to core")

            // Create account parameters
            val accountParams = core.createAccountParams()

            // Set identity address
            val identity = Factory.instance().createAddress("sip:$username@${Constants.DOMAIN}")
            if (identity == null) {
                Log.e(AppConstants.TAG_SIP_MANAGER, "Failed to create identity address")
                return
            }
            accountParams.identityAddress = identity
            Log.d(AppConstants.TAG_SIP_MANAGER, "Identity set to: ${identity.asStringUriOnly()}")

            // Set server address - FORCE UDP transport for Asterisk
            val serverAddress = Factory.instance().createAddress("sip:${Constants.DOMAIN}:5060;transport=udp")
            if (serverAddress == null) {
                Log.e(AppConstants.TAG_SIP_MANAGER, "Failed to create server address")
                return
            }
            accountParams.serverAddress = serverAddress
            Log.d(AppConstants.TAG_SIP_MANAGER, "Server address set to: ${serverAddress.asStringUriOnly()} with transport UDP")

            // Enable registration
            accountParams.isRegisterEnabled = true
            accountParams.expires = 600
            accountParams.publishExpires = -1

            // Disable push notifications
            accountParams.pushNotificationAllowed = false
            accountParams.remotePushNotificationAllowed = false

            // Asterisk compatibility settings
            accountParams.avpfMode = AVPFMode.Disabled

            // CRITICAL: Force UDP transport only
            accountParams.setTransport(TransportType.Udp)

            // NAT policy
            val natPolicy = core.createNatPolicy()
            natPolicy.isStunEnabled = false
            natPolicy.isIceEnabled = false
            accountParams.natPolicy = natPolicy

            Log.d(AppConstants.TAG_SIP_MANAGER, "Account parameters configured")

            // Create and add account
            val account = core.createAccount(accountParams)
            if (account == null) {
                Log.e(AppConstants.TAG_SIP_MANAGER, "Failed to create account")
                return
            }

            core.addAccount(account)
            core.defaultAccount = account

            Log.i(AppConstants.TAG_SIP_MANAGER, "Account created, registration will start automatically")

        } catch (e: Exception) {
            Log.e(AppConstants.TAG_SIP_MANAGER, "Exception during login: ${e.message}", e)
            e.printStackTrace()
        }
    }

    /**
     * Logout/Unregister from SIP server
     */
    fun logout() {
        Log.d(AppConstants.TAG_SIP_MANAGER, "Logging out")
        core.defaultAccount?.let { account ->
            val params = account.params.clone()
            params.isRegisterEnabled = false
            account.params = params
        }
        core.clearAccounts()
        core.clearAllAuthInfo()
        resetCallState()
        Log.i(AppConstants.TAG_SIP_MANAGER, "Logged out successfully")
    }

    /**
     * Make an outgoing call
     */
    fun call(number1: String) {
        val number = number1.replace("[^0-9+]".toRegex(), "").replace(" ", "").trim()
        Log.d(AppConstants.TAG_SIP_MANAGER, "Attempting to call: $number")

        if (!isRegistered()) {
            Log.e(AppConstants.TAG_SIP_MANAGER, "Cannot make call: Not registered")
            return
        }

        val intent = Intent(context, CallingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AppConstants.PHONE_NUMBER, number)
            putExtra(AppConstants.CALL_TYPE, AppConstants.CALL_TYPE_OUTGOING)
        }
        context.startActivity(intent)

        val remoteAddress = Factory.instance().createAddress("sip:$number@${Constants.DOMAIN}")
        if (remoteAddress != null) {
            val params = core.createCallParams(null)
            params?.let {
                val call = core.inviteAddressWithParams(remoteAddress, it)
                if (call != null) {
                    Log.i(AppConstants.TAG_SIP_MANAGER, "Call initiated to $number")
                } else {
                    Log.e(AppConstants.TAG_SIP_MANAGER, "Failed to initiate call")
                }
            }
        } else {
            Log.e(AppConstants.TAG_SIP_MANAGER, "Failed to create remote address for $number")
        }
    }

    /**
     * Answer an incoming call
     */
    fun answerCall() {
        Log.d(AppConstants.TAG_SIP_MANAGER, "Answering call")
        core.currentCall?.let { call ->
            call.accept()
            Log.i(AppConstants.TAG_SIP_MANAGER, "Call answered")
        } ?: Log.w(AppConstants.TAG_SIP_MANAGER, "No current call to answer")
    }

    /**
     * Hangup the current call
     */
    fun hangup() {
        Log.d(AppConstants.TAG_SIP_MANAGER, "Hanging up call")
        core.currentCall?.let { call ->
            call.terminate()
            Log.i(AppConstants.TAG_SIP_MANAGER, "Call terminated")
        } ?: Log.w(AppConstants.TAG_SIP_MANAGER, "No current call to hang up")
    }

    /**
     * Hangup all calls
     */
    fun hangupAll() {
        Log.d(AppConstants.TAG_SIP_MANAGER, "Hanging up all calls")
        core.terminateAllCalls()
    }

    /**
     * Toggle microphone mute
     */
    fun toggleMicrophoneMute() {
        val isMuted = !core.isMicEnabled
        core.isMicEnabled = !isMuted
        Log.d(AppConstants.TAG_SIP_MANAGER, "Microphone ${if (isMuted) "muted" else "unmuted"}")
    }

    /**
     * Toggle speaker
     */
    fun toggleSpeaker() {
        val currentDevice = core.currentCall?.outputAudioDevice
        val audioDevices = core.audioDevices

        val speakerDevice = audioDevices.find {
            it.type == AudioDevice.Type.Speaker
        }
        val earPieceDevice = audioDevices.find {
            it.type == AudioDevice.Type.Earpiece
        }

        val newDevice = if (currentDevice?.type == AudioDevice.Type.Speaker) {
            earPieceDevice ?: currentDevice
        } else {
            speakerDevice ?: currentDevice
        }

        core.currentCall?.outputAudioDevice = newDevice
        Log.d(AppConstants.TAG_SIP_MANAGER, "Audio device changed to: ${newDevice?.deviceName}")
    }

    /**
     * Send DTMF tone
     */
    fun sendDtmf(digit: Char) {
        core.currentCall?.sendDtmf(digit)
        Log.d(AppConstants.TAG_SIP_MANAGER, "DTMF sent: $digit")
    }

    /**
     * Check if registered
     */
    fun isRegistered(): Boolean {
        return core.defaultAccount?.state == RegistrationState.Ok
    }

    /**
     * Check if in a call
     */

    fun isInCall(): Boolean {
        return core.callsNb > 0
    }

    /**
     * Get current call state
     */
    fun getCurrentCallState(): Call.State? {
        return core.currentCall?.state
    }

    fun resetCallState() {
        _callState.value = CallState.State(CallState.Idle, CallState.RegistrationState.Idle)
    }

    companion object {
        private const val TAG = AppConstants.TAG_SIP_MANAGER
    }
}
