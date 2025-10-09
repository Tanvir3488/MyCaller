package com.bnw.voip.voip

import org.linphone.core.Call

data class CallStateEvent(val state: Call.State?, val call: Call?)
