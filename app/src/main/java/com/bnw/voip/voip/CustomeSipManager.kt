package com.bnw.voip.voip

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bnw.voip.utils.CallNotificationManager
import com.bnw.voip.voip.Constants
import org.linphone.core.*


class CustomeSipManager(private val context: Context) {
    private val core: Core
    private val handler = Handler(Looper.getMainLooper())
    private var isStarted = false

    private val iterateRunnable = object : Runnable {
        override fun run() {
            if (isStarted) {
                core.iterate()
                handler.postDelayed(this, 20) // Iterate every 20ms
            }
        }
    }

    init {
        Log.d(TAG, "Initializing SipManager")

        val factory = Factory.instance()
        factory.setDebugMode(true, "Linphone")

        core = factory.createCore(null, null, context)

        // Configure transports - Disable TLS completely
        core.transports?.apply {
            udpPort = 0      // Random port for UDP
            tcpPort = 0      // Random port for TCP
            tlsPort = -1     // DISABLE TLS completely
        }

        Log.d(TAG, "Transports configured - UDP: enabled, TCP: enabled, TLS: DISABLED")

        // Configure audio settings
        core.isEchoCancellationEnabled = true
        core.isAdaptiveRateControlEnabled = true

        // Configure network settings for Asterisk compatibility
        core.isIpv6Enabled = false // Disable IPv6 if Asterisk doesn't support it
        core.setUserAgent("T", core.version)
        core.isKeepAliveEnabled = true
        core.guessHostname = true

        // Add core listener
        core.addListener(object : CoreListenerStub() {
            override fun onAccountRegistrationStateChanged(
                core: Core,
                account: Account,
                state: RegistrationState,
                message: String
            ) {
                Log.d(TAG, "Registration state: $state, message: $message")
                when (state) {
                    RegistrationState.Ok -> {
                        Log.i(TAG, "Registration successful")
                        onRegistrationSuccess()
                    }
                    RegistrationState.Failed -> {
                        Log.e(TAG, "Registration failed: $message")
                        onRegistrationFailed(message)
                    }
                    RegistrationState.Progress -> {
                        Log.d(TAG, "Registration in progress")
                    }
                    RegistrationState.Cleared -> {
                        Log.d(TAG, "Registration cleared")
                    }
                    else -> {
                        Log.d(TAG, "Registration state: $state")
                    }
                }
            }

            override fun onCallStateChanged(
                core: Core,
                call: Call,
                state: Call.State,
                message: String
            ) {
                Log.d(TAG, "Call state changed: $state, message: $message")
                when (state) {
                    Call.State.IncomingReceived -> {
                        Log.i(TAG, "Incoming call received")
                        onIncomingCall(call)
                    }
                    Call.State.OutgoingInit -> {
                        Log.i(TAG, "Outgoing call initiated")
                    }
                    Call.State.OutgoingProgress -> {
                        Log.i(TAG, "Outgoing call in progress")
                    }
                    Call.State.OutgoingRinging -> {
                        Log.i(TAG, "Remote ringing")
                    }
                    Call.State.Connected -> {
                        Log.i(TAG, "Call connected")
                        onCallConnected(call)
                    }
                    Call.State.StreamsRunning -> {
                        Log.i(TAG, "Call streams running (audio/video active)")
                        onCallActive(call)
                    }
                    Call.State.Released -> {
                        Log.i(TAG, "Call released")
                        onCallEnded(call)
                    }
                    Call.State.Error -> {
                        Log.e(TAG, "Call error: $message")
                        onCallError(call, message)
                    }
                    else -> {
                        Log.d(TAG, "Call state: $state")
                    }
                }
            }

            override fun onGlobalStateChanged(
                core: Core,
                state: GlobalState,
                message: String
            ) {
                Log.d(TAG, "Global state changed: $state, message: $message")
            }

            override fun onNetworkReachable(core: Core, reachable: Boolean) {
                Log.d(TAG, "Network reachable: $reachable")
                if (!reachable) {
                    Log.w(TAG, "Network is not reachable!")
                }
            }

            override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {
                Log.d(TAG, "Audio device changed: ${audioDevice.deviceName}")
            }

            override fun onCallStatsUpdated(core: Core, call: Call, stats: CallStats) {
                // Uncomment for detailed call statistics
                // Log.v(TAG, "Call stats updated")
            }
        })

        Log.d(TAG, "SipManager initialized successfully")
    }

    /**
     * Start the Linphone core
     */
    fun start() {
        if (isStarted) {
            Log.w(TAG, "SipManager already started")
            return
        }

        Log.d(TAG, "Starting SipManager")
        core.start()
        isStarted = true
        handler.post(iterateRunnable)
        Log.i(TAG, "SipManager started successfully")
    }

    /**
     * Stop the Linphone core
     */
    fun stop() {
        if (!isStarted) {
            Log.w(TAG, "SipManager already stopped")
            return
        }

        Log.d(TAG, "Stopping SipManager")
        isStarted = false
        handler.removeCallbacks(iterateRunnable)

        // Hangup all calls
        if (core.callsNb > 0) {
            core.terminateAllCalls()
        }

        core.stop()
        Log.i(TAG, "SipManager stopped successfully")
    }

    /**
     * Login/Register with SIP server
     */
    fun login() {
        try {
            Log.d(TAG, "Attempting login - Username: ${Constants.USERNAME}, Domain: ${Constants.DOMAIN}")

            // Create authentication info
            val authInfo = Factory.instance().createAuthInfo(
                Constants.USERNAME,    // username
                Constants.USERNAME,    // userid
                Constants.PASSWORD,    // password
                null,                  // ha1
                null,                  // realm (null = any realm)
                Constants.DOMAIN       // domain
            )
            core.addAuthInfo(authInfo)
            Log.d(TAG, "AuthInfo added to core")

            // Create account parameters
            val accountParams = core.createAccountParams()

            // Set identity address
            val identity = Factory.instance().createAddress("sip:${Constants.USERNAME}@${Constants.DOMAIN}")
            if (identity == null) {
                Log.e(TAG, "Failed to create identity address")
                return
            }
            accountParams.identityAddress = identity
            Log.d(TAG, "Identity set to: ${identity.asStringUriOnly()}")

            // Set server address - FORCE UDP transport for Asterisk
            val serverAddress = Factory.instance().createAddress("sip:${Constants.DOMAIN}:5060;transport=udp")
            if (serverAddress == null) {
                Log.e(TAG, "Failed to create server address")
                return
            }
            accountParams.serverAddress = serverAddress
            Log.d(TAG, "Server address set to: ${serverAddress.asStringUriOnly()} with transport UDP")

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

            Log.d(TAG, "Account parameters configured")

            // Create and add account
            val account = core.createAccount(accountParams)
            if (account == null) {
                Log.e(TAG, "Failed to create account")
                return
            }

            core.addAccount(account)
            core.defaultAccount = account

            Log.i(TAG, "Account created, registration will start automatically")

        } catch (e: Exception) {
            Log.e(TAG, "Exception during login: ${e.message}", e)
            e.printStackTrace()
        }
    }

    /**
     * Logout/Unregister from SIP server
     */
    fun logout() {
        Log.d(TAG, "Logging out")
        core.defaultAccount?.let { account ->
            val params = account.params.clone()
            params.isRegisterEnabled = false
            account.params = params
        }
        core.clearAccounts()
        core.clearAllAuthInfo()
        Log.i(TAG, "Logged out successfully")
    }

    /**
     * Make an outgoing call
     */
    fun call(number1: String) {
        val number = number1.replace("[^0-9+]", "").replace(" ","").trim()
        Log.d(TAG, "Attempting to call: $number")

        if (!isRegistered()) {
            Log.e(TAG, "Cannot make call: Not registered")
            return
        }

        val remoteAddress = Factory.instance().createAddress("sip:$number@${Constants.DOMAIN}")
        if (remoteAddress != null) {
            val params = core.createCallParams(null)
            params?.let {
                val call = core.inviteAddressWithParams(remoteAddress, it)
                if (call != null) {
                    Log.i(TAG, "Call initiated to $number")
                } else {
                    Log.e(TAG, "Failed to initiate call")
                }
            }
        } else {
            Log.e(TAG, "Failed to create remote address for $number")
        }
    }

    /**
     * Answer an incoming call
     */
    fun answerCall() {
        Log.d(TAG, "Answering call")
        core.currentCall?.let { call ->
            call.accept()
            Log.i(TAG, "Call answered")
        } ?: Log.w(TAG, "No current call to answer")
    }

    /**
     * Hangup the current call
     */
    fun hangup() {
        Log.d(TAG, "Hanging up call")
        core.currentCall?.let { call ->
            call.terminate()
            Log.i(TAG, "Call terminated")
        } ?: Log.w(TAG, "No current call to hang up")
    }

    /**
     * Hangup all calls
     */
    fun hangupAll() {
        Log.d(TAG, "Hanging up all calls")
        core.terminateAllCalls()
    }

    /**
     * Toggle microphone mute
     */
    fun toggleMicrophoneMute() {
        val isMuted = !core.isMicEnabled
        core.isMicEnabled = !isMuted
        Log.d(TAG, "Microphone ${if (isMuted) "muted" else "unmuted"}")
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
        Log.d(TAG, "Audio device changed to: ${newDevice?.deviceName}")
    }

    /**
     * Send DTMF tone
     */
    fun sendDtmf(digit: Char) {
        core.currentCall?.sendDtmf(digit)
        Log.d(TAG, "DTMF sent: $digit")
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

    // Callback methods - override these in your implementation
    protected open fun onRegistrationSuccess() {
        // Override in subclass or use listeners
    }

    protected open fun onRegistrationFailed(message: String) {
        // Override in subclass or use listeners
    }

    protected open fun onIncomingCall(call: Call) {
        val callNotificationManager = CallNotificationManager(context)
        callNotificationManager.showIncomingCall(call.remoteAddress.displayName ?: "Unknown")
       // call.accept()
    }

    protected open fun onCallConnected(call: Call) {
        // Override in subclass or use listeners
    }

    protected open fun onCallActive(call: Call) {
        // Override in subclass or use listeners
    }

    protected open fun onCallEnded(call: Call) {
        // Override in subclass or use listeners
    }

    protected open fun onCallError(call: Call, message: String) {
        // Override in subclass or use listeners
    }

    companion object {
        private const val TAG = "SipManager"
    }
}
