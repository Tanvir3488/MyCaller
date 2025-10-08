package com.bnw.voip

import android.app.Application
import com.bnw.voip.voip.CustomeSipManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    companion object{
        lateinit var sipManager : CustomeSipManager
    }

    override fun onCreate() {
        super.onCreate()
        sipManager = CustomeSipManager(this)
    }

}
