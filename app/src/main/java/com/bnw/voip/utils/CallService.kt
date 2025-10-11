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
import com.bnw.voip.domain.usecase.call.GetCallStateUseCase
import com.bnw.voip.domain.usecase.call.LoginUseCase
import com.bnw.voip.domain.usecase.call.StartSipUseCase
import com.bnw.voip.ui.main.MainActivity
import com.bnw.voip.voip.CallTracker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.linphone.core.Call
import javax.inject.Inject

/******

 **** Created By  TANVIR3488 AT 8/10/25 10:54 PM

 ******/

@AndroidEntryPoint
class CallService : Service() {

    @Inject
    lateinit var loginUseCase: LoginUseCase

    @Inject
    lateinit var callTracker: CallTracker
    @Inject
    lateinit var startSipUseCase: StartSipUseCase
    @Inject
    lateinit var getCallStateUseCase: GetCallStateUseCase
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
        loginUseCase()
        startSipUseCase()
        callTracker.startTracking()
        serviceScope.launch {
            getCallStateUseCase().collect {
                android.util.Log.d("CallService", "Call state received: ${it?.state}") // Added log
                if (it?.state == Call.State.IncomingReceived) {
                    android.util.Log.d("CallService", "Incoming call received, starting vibration") // Added log
                    callNotificationManager.showIncomingCall(it.call?.remoteAddress?.displayName ?: "Unknown")
                    val pattern = longArrayOf(0, 1000, 500) // Vibrate for 1s, pause for 0.5s
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0)) // Repeat from index 0
                    } else {
                        vibrator.vibrate(pattern, 0) // Repeat from index 0
                    }
                } else if (it?.state == Call.State.End || it?.state == Call.State.Released || it?.state == Call.State.Connected || it?.state == Call.State.StreamsRunning) {
                    android.util.Log.d("CallService", "Call connected, ended or released, stopping vibration") // Added log
                    callNotificationManager.dismissNotification()
                    vibrator.cancel()
                }
            }
        }
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
