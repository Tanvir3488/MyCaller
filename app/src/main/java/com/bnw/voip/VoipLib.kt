package com.bnw.voip

/******

 **** Created By  TANVIR3488 AT 28/9/25 10:07 PM

 ******/


object VoipLib {
    init {
        System.loadLibrary("pjsip_jni")
    }
    external fun init()
}

