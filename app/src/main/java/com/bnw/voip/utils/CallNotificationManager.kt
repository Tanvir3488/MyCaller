package com.bnw.voip.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bnw.voip.R
import com.bnw.voip.ui.incommingcall.IncomingCallActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/******
 **** Created By  TANVIR3488 AT 8/10/25 10:40 PM
 ******/

@Singleton
class CallNotificationManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val channelId = "call_channel1"
    private val notificationId = 1003

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Call Notifications1",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming call alerts1"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                // Enable sound and vibration for better alerting
                enableVibration(true)
                setBypassDnd(true) // Bypass Do Not Disturb
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showIncomingCall(callerName: String, callId: String = "") {
        // Check notification permission first (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        // Full-screen intent for locked/sleeping phone
        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("caller_name", callerName)
            putExtra("call_id", callId)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Content intent (when tapped from notification drawer)
        val contentIntent = PendingIntent.getActivity(
            context,
            1,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Answer action
        val answerIntent = Intent(context, IncomingCallActivity::class.java).apply {
            action = "ACTION_ANSWER_CALL"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("caller_name", callerName)
            putExtra("call_id", callId)
        }
        val answerPendingIntent = PendingIntent.getActivity(
            context,
            2,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Decline action
        val declineIntent = Intent(context, IncomingCallActivity::class.java).apply {
            action = "ACTION_DECLINE_CALL"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("call_id", callId)
        }
        val declinePendingIntent = PendingIntent.getActivity(
            context,
            3,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Incoming Call")
            .setContentText("Call from $callerName")
            .setSmallIcon(R.drawable.ic_call)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX) // MAX for full-screen
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true) // Critical for lock screen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on lock screen
            .setVibrate(longArrayOf(0, 1000, 500, 1000)) // Vibration pattern
            .setSound(null) // Use your custom ringtone if needed
            .addAction(
                R.drawable.ic_call,
                "Decline",
                declinePendingIntent
            )
            .addAction(
                R.drawable.ic_call,
                "Answer",
                answerPendingIntent
            )
            .build()

        val manager = NotificationManagerCompat.from(context)
        manager.notify(notificationId, notification)
    }

    fun dismissNotification() {
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(notificationId)
    }
}
