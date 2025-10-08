package com.bnw.voip.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bnw.voip.MyApplication.Companion.sipManager
import com.bnw.voip.R
import com.bnw.voip.ui.main.MainActivity
import com.bnw.voip.voip.CustomeSipManager

/******

 **** Created By  TANVIR3488 AT 8/10/25 10:54 PM

 ******/


class CallService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        // Not using binding here
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    private fun startForegroundService() {
        val channelId = "call_service_channel"

        // Create Notification Channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Call Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Intent to reopen your app when notification is clicked
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Persistent notification for the service
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Call Service Running")
            .setContentText("Listening for incoming calls…")
            .setSmallIcon(R.drawable.ic_call)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        // Start service in the foreground
        startForeground(1, notification)



        sipManager.login()
        sipManager.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup if needed
    }
}

