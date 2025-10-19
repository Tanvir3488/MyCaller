package com.bnw.voip.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import com.bnw.voip.R
import com.bnw.voip.data.datastore.UserManager
import com.bnw.voip.domain.usecase.call.GetCallStateUseCase
import com.bnw.voip.domain.usecase.call.LoginUseCase
import com.bnw.voip.domain.usecase.call.StartSipUseCase
import com.bnw.voip.ui.main.MainActivity
import com.bnw.voip.voip.CallState
import com.bnw.voip.voip.CallTracker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallService : Service() {

    @Inject
    lateinit var loginUseCase: LoginUseCase

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var callTracker: CallTracker
    @Inject
    lateinit var startSipUseCase: StartSipUseCase
    @Inject
    lateinit var callNotificationManager: CallNotificationManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var vibrator: Vibrator

    override fun onBind(intent: Intent?): IBinder? {
        // Not using binding here
        return null
    }

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        startForegroundService()
        serviceScope.launch {
            val username = userManager.usernameFlow.first()
            val password = userManager.passwordFlow.first()
            if (username != null && password != null) {
                loginUseCase(username, password)
            }
        }
        startSipUseCase()
        callTracker.startTracking()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        vibrator.cancel()
        serviceScope.cancel()
    }
}
