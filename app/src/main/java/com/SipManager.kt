package com

import android.content.Context
import android.util.Log
import org.linphone.core.*

class SipManager(context: Context) {
    private val core: Core

    init {
        Log.e("PJSip", "SipManager: Initializing SipManager")
        val factory = Factory.instance()
        core = factory.createCore(null, null, context)
        core.addListener(object : CoreListener {
            override fun onAccountRegistrationStateChanged(core: Core, account: Account, state: RegistrationState, message: String) {
                Log.e("Callback","onAccountRegistrationStateChanged: state=$state, message=$message")
                if (state == RegistrationState.Ok) {
                    Log.e("Callback", "LOGIN SUCCESS")
                } else if (state == RegistrationState.Failed) {
                    Log.e("Callback", "LOGIN FAILED: $message")
                }
            }

            override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {
                Log.e("Callback", "onAudioDeviceChanged")
            }

            override fun onSubscribeReceived(core: Core, linphoneEvent: Event, subscribeEvent: String, body: Content) {
                Log.e("Callback", "onSubscribeReceived")
            }

            override fun onCallStateChanged(core: Core, call: Call, state: Call.State, message: String) {
                Log.e("Callback","onCallStateChanged: state=$state, message=$message")
            }

            override fun onMessagesReceived(core: Core, chatRoom: ChatRoom, messages: Array<out ChatMessage?>) {
                Log.e("Callback", "onMessagesReceived")
            }

            override fun onCallCreated(core: Core, call: Call) {
                Log.e("Callback", "onCallCreated")
            }

            override fun onPushNotificationReceived(core: Core, payload: String?) {
                Log.e("Callback", "onPushNotificationReceived")
            }

            override fun onInfoReceived(core: Core, call: Call, message: InfoMessage) {
                Log.e("Callback", "onInfoReceived")
            }

            override fun onNotifyPresenceReceivedForUriOrTel(core: Core, linphoneFriend: Friend, uriOrTel: String, presenceModel: PresenceModel) {
                Log.e("Callback", "onNotifyPresenceReceivedForUriOrTel")
            }

            override fun onLogCollectionUploadProgressIndication(core: Core, offset: Int, total: Int) {
                Log.e("Callback", "onLogCollectionUploadProgressIndication")
            }

            override fun onCallStatsUpdated(core: Core, call: Call, stats: CallStats) {
                Log.e("Callback", "onCallStatsUpdated")
            }

            override fun onChatRoomStateChanged(core: Core, room: ChatRoom, state: ChatRoom.State) {
                Log.e("Callback", "onChatRoomStateChanged: state=$state")
            }

            override fun onEcCalibrationAudioInit(core: Core) {
                Log.e("Callback", "onEcCalibrationAudioInit")
            }

            override fun onCallEncryptionChanged(core: Core, call: Call, mediaEncryptionEnabled: Boolean, authenticationToken: String?) {
                Log.e("Callback", "onCallEncryptionChanged")
            }

            override fun onNotifyPresenceReceived(core: Core, linphoneFriend: Friend) {
                Log.e("Callback", "onNotifyPresenceReceived")
            }

            override fun onTransferStateChanged(core: Core, transfered: Call, callState: Call.State?) {
                Log.e("Callback", "onTransferStateChanged")
            }

            override fun onNewSubscriptionRequested(core: Core, linphoneFriend: Friend, url: String) {
                Log.e("Callback", "onNewSubscriptionRequested")
            }

            override fun onConfiguringStatus(core: Core, status: ConfiguringState?, message: String?) {
                Log.e("Callback", "onConfiguringStatus")
            }

            override fun onConferenceStateChanged(core: Core, conference: Conference, state: Conference.State) {
                Log.e("Callback", "onConferenceStateChanged: state=$state")
            }

            override fun onEcCalibrationResult(core: Core, status: EcCalibratorStatus, delayMs: Int) {
                Log.e("Callback", "onEcCalibrationResult: status=$status, delayMs=$delayMs")
            }

            override fun onAudioDevicesListUpdated(core: Core) {
                Log.e("Callback", "onAudioDevicesListUpdated")
            }

            override fun onFriendListCreated(core: Core, friendList: FriendList) {
                Log.e("Callback", "onFriendListCreated")
            }

            override fun onGlobalStateChanged(core: Core, state: GlobalState, message: String) {
                Log.e("Callback", "onGlobalStateChanged: state=$state, message=$message")
            }

            override fun onIsComposingReceived(core: Core, room: ChatRoom) {
                Log.e("Callback", "onIsComposingReceived")
            }

            override fun onAuthenticationRequested(core: Core, authInfo: AuthInfo, method: AuthMethod) {
                Log.e("Callback", "onAuthenticationRequested")
            }

            override fun onCallLogUpdated(core: Core, callLog: CallLog) {
                Log.e("Callback", "onCallLogUpdated")
            }

            override fun onMessageReceived(core: Core, room: ChatRoom, message: ChatMessage) {
                Log.e("Callback", "onMessageReceived")
            }

            override fun onMessageReceivedUnableDecrypt(core: Core, room: ChatRoom, message: ChatMessage) {
                Log.e("Callback", "onMessageReceivedUnableDecrypt")
            }

            override fun onLogCollectionUploadStateChanged(core: Core, state: Core.LogCollectionUploadState?, info: String) {
                Log.e("Callback", "onLogCollectionUploadStateChanged")
            }

            override fun onImeeUserRegistration(core: Core, status: Boolean, userId: String, info: String) {
                Log.e("Callback", "onImeeUserRegistration")
            }

            override fun onChatRoomRead(core: Core, chatRoom: ChatRoom) {
                Log.e("Callback", "onChatRoomRead")
            }

            override fun onNetworkReachable(core: Core, reachable: Boolean) {
                Log.e("Callback", "onNetworkReachable: reachable=$reachable")
            }

            override fun onChatRoomSubjectChanged(core: Core, chatRoom: ChatRoom) {
                Log.e("Callback", "onChatRoomSubjectChanged")
            }

            override fun onEcCalibrationAudioUninit(core: Core) {
                Log.e("Callback", "onEcCalibrationAudioUninit")
            }

            override fun onChatRoomEphemeralMessageDeleted(core: Core, chatRoom: ChatRoom) {
                Log.e("Callback", "onChatRoomEphemeralMessageDeleted")
            }

            override fun onDtmfReceived(core: Core, call: Call, dtmf: Int) {
                Log.e("Callback", "onDtmfReceived")
            }

            override fun onConferenceInfoReceived(core: Core, conferenceInfo: ConferenceInfo) {
                Log.e("Callback", "onConferenceInfoReceived")
            }

            override fun onCallIdUpdated(core: Core, previousCallId: String, currentCallId: String) {
                Log.e("Callback", "onCallIdUpdated")
            }

            override fun onVersionUpdateCheckResultReceived(core: Core, result: VersionUpdateCheckResult, version: String?, url: String?) {
                Log.e("Callback", "onVersionUpdateCheckResultReceived")
            }

            override fun onQrcodeFound(core: Core, result: String?) {
                Log.e("Callback", "onQrcodeFound")
            }

            override fun onRegistrationStateChanged(core: Core, proxyConfig: ProxyConfig, state: RegistrationState?, message: String) {
                Log.e("Callback", "onRegistrationStateChanged")
            }

            override fun onNotifySent(core: Core, linphoneEvent: Event, body: Content) {
                Log.e("Callback", "onNotifySent")
            }

            override fun onNotifyReceived(core: Core, event: Event, notifiedEvent: String, body: Content) {
                Log.e("Callback", "onNotifyReceived")
            }

            override fun onFriendListRemoved(core: Core, friendList: FriendList) {
                Log.e("Callback", "onFriendListRemoved")
            }

            override fun onMessageSent(core: Core, chatRoom: ChatRoom, message: ChatMessage) {
                Log.e("Callback", "onMessageSent")
            }

            override fun onFirstCallStarted(core: Core) {
                Log.e("Callback", "onFirstCallStarted")
            }

            override fun onLastCallEnded(core: Core) {
                Log.e("Callback", "onLastCallEnded")
            }

            override fun onPublishStateChanged(core: Core, event: Event, state: PublishState) {
                Log.e("Callback", "onPublishStateChanged: state=$state")
            }

            override fun onCallGoclearAckSent(core: Core, call: Call) {
                Log.e("Callback", "onCallGoclearAckSent")
            }

            override fun onBuddyInfoUpdated(core: Core, linphoneFriend: Friend) {
                Log.e("Callback", "onBuddyInfoUpdated")
            }

            override fun onReferReceived(core: Core, referTo: String) {
                Log.e("Callback", "onReferReceived: referTo=$referTo")
            }

            override fun onSubscriptionStateChanged(core: Core, event: Event, state: SubscriptionState) {
                Log.e("Callback", "onSubscriptionStateChanged: state=$state")
            }
        })
    }

    fun start() {
        Log.e("PJSip", "SipManager: Starting core")
        core.start()
    }

    fun login() {
        Log.e("PJSip", "SipManager: Attempting to login with username: ${Constants.USERNAME}, domain: ${Constants.DOMAIN}")
        val authInfo = Factory.instance().createAuthInfo(Constants.USERNAME, null, Constants.PASSWORD, null, null, Constants.DOMAIN)
        core.addAuthInfo(authInfo)
        Log.e("PJSip", "SipManager: AuthInfo added")

        val accountParams = core.createAccountParams()
        val identity = Factory.instance().createAddress("sip:${Constants.USERNAME}@${Constants.DOMAIN}")
        accountParams.identityAddress = identity
        accountParams.serverAddress = Factory.instance().createAddress("sip:${Constants.DOMAIN}:5060;transport=udp")
        accountParams.setRegisterEnabled(true)
        Log.e("PJSip", "SipManager: AccountParams configured")

        val account = core.createAccount(accountParams)
        core.defaultAccount = account
        Log.e("PJSip", "SipManager: Account created and set as default")
    }

    fun call(number: String) {
        Log.e("PJSip", "SipManager: Attempting to call number: $number")
        val remoteAddress = Factory.instance().createAddress("sip:$number@${Constants.DOMAIN}")
        if (remoteAddress != null) {
            core.inviteAddress(remoteAddress)
            Log.e("PJSip", "SipManager: Invite sent to $remoteAddress")
        } else {
            Log.e("PJSip", "SipManager: Failed to create remote address for $number")
        }
    }
}
