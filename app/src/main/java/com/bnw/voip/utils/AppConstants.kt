package com.bnw.voip.utils

object AppConstants {
    // Intent Keys
    const val PHONE_NUMBER = "phone_number"
    const val CALL_ID = "call_id"
    const val CALL_TYPE = "Call_Type"

    // Call Types
    const val CALL_TYPE_OUTGOING = "OUTGOING"
    const val CALL_TYPE_INCOMING = "INCOMING"
    const val CALL_TYPE_ANSWERED = "answered"
    const val CALL_TYPE_MISSED = "missed"

    // Notification Constants
    const val CALL_CHANNEL_ID = "call_channel1"
    const val CALL_NOTIFICATION_ID = 1003
    const val CALL_CHANNEL_NAME = "Call Notifications1"
    const val CALL_CHANNEL_DESCRIPTION = "Incoming call alerts1"
    const val ACTION_ANSWER_CALL = "ACTION_ANSWER_CALL"
    const val ACTION_DECLINE_CALL = "ACTION_DECLINE_CALL"
    const val INCOMING_CALL_TITLE = "Incoming Call"

    // UI Constants
    const val UNKNOWN_CALLER = "Unknown"

    // Logging Tags
    const val TAG_SIP_MANAGER = "SipManager"
}
