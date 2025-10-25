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
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bnw.voip.R
import com.bnw.voip.ui.incommingcall.CallingActivity
import com.bnw.voip.utils.AppConstants.CALL_TYPE_INCOMING
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/******
 **** Created By  TANVIR3488 AT 8/10/25 10:40 PM
 ******/

@Singleton
class CallNotificationManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConstants.CALL_CHANNEL_ID,
                "VoIP Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming and ongoing VoIP calls"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200)
                setBypassDnd(true)
                enableLights(true)
                lightColor = context.getColor(R.color.primary_purple)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            val missedCallChannel = NotificationChannel(
                AppConstants.MISSED_CALL_CHANNEL_ID,
                "Missed Calls",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for missed VoIP calls"
                enableLights(true)
                lightColor = context.getColor(R.color.missed_call_red)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
            }
            manager.createNotificationChannel(missedCallChannel)
        }
    }

    fun showIncomingCall(phoneNumber: String) {
        Log.e("CallNotificationManager", "Showing incoming call notification for $phoneNumber")
        
        // Start continuous vibration for incoming call
        startIncomingCallVibration()
        
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
        val fullScreenIntent = Intent(context, CallingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AppConstants.PHONE_NUMBER, phoneNumber)
            putExtra(AppConstants.CALL_TYPE, CALL_TYPE_INCOMING)
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
        val answerIntent = Intent(context, CallingActivity::class.java).apply {
            action = AppConstants.ACTION_ANSWER_CALL
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AppConstants.PHONE_NUMBER, phoneNumber)
            putExtra(AppConstants.CALL_TYPE, CALL_TYPE_INCOMING)
        }
        val answerPendingIntent = PendingIntent.getActivity(
            context,
            2,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Decline action
        val declineIntent = Intent(context, CallingActivity::class.java).apply {
            action = AppConstants.ACTION_DECLINE_CALL
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AppConstants.PHONE_NUMBER, phoneNumber)
            putExtra(AppConstants.CALL_TYPE, CALL_TYPE_INCOMING)
        }
        val declinePendingIntent = PendingIntent.getActivity(
            context,
            3,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AppConstants.CALL_CHANNEL_ID)
            .setContentTitle("📞 Incoming Call")
            .setContentText("Call from $phoneNumber")
            .setSubText("VoIP Call")
            .setSmallIcon(R.drawable.ic_call)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(null)
            .setColorized(true)
            .setColor(context.getColor(R.color.primary_purple))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Incoming VoIP call from $phoneNumber\nTap to answer or use the action buttons below.")
                .setBigContentTitle("📞 Incoming Call")
                .setSummaryText("VoIP Call"))
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_call_end,
                    "Decline",
                    declinePendingIntent
                ).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_call,
                    "Answer",
                    answerPendingIntent
                ).build()
            )
            .build()

        val manager = NotificationManagerCompat.from(context)
        manager.notify(AppConstants.CALL_NOTIFICATION_ID, notification)
    }

    fun showOngoingCallNotification(contactName: String?, phoneNumber: String, connectedWallTime: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val declineIntent = Intent(context, CallingActivity::class.java).apply {
            action = AppConstants.ACTION_DECLINE_CALL
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val declinePendingIntent = PendingIntent.getActivity(
            context,
            3,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = Intent(context, CallingActivity::class.java).apply {
            putExtra(AppConstants.PHONE_NUMBER, phoneNumber)
            putExtra(AppConstants.CALL_TYPE, AppConstants.CALL_TYPE_ONGOING)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            4,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AppConstants.CALL_CHANNEL_ID)
            .setContentTitle("🔊 ${contactName ?: phoneNumber}")
            .setContentText("VoIP call in progress")
            .setSubText("Tap to return to call")
            .setSmallIcon(R.drawable.ic_call)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(false)
            .setWhen(connectedWallTime)
            .setUsesChronometer(true)
            .setContentIntent(contentPendingIntent)
            .setColorized(true)
            .setColor(context.getColor(R.color.primary_purple))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Active VoIP call with ${contactName ?: phoneNumber}\nTap to return to call or use the hang up button.")
                .setBigContentTitle("🔊 Call in Progress")
                .setSummaryText("VoIP Call"))
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_call_end,
                    "Hang Up",
                    declinePendingIntent
                ).build()
            )
            .build()
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT

        val manager = NotificationManagerCompat.from(context)
        manager.notify(AppConstants.CALL_NOTIFICATION_ID, notification)
    }

    fun showMissedCallNotification(contactName: String?, phoneNumber: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, com.bnw.voip.ui.main.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, AppConstants.MISSED_CALL_CHANNEL_ID)
            .setContentTitle("📞 Missed Call")
            .setContentText("From ${contactName ?: phoneNumber}")
            .setSubText("VoIP Call")
            .setSmallIcon(R.drawable.ic_call_missed)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColorized(true)
            .setColor(context.getColor(R.color.missed_call_red))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You missed a VoIP call from ${contactName ?: phoneNumber}\nTap to view call history and call back.")
                .setBigContentTitle("📞 Missed Call")
                .setSummaryText("VoIP Call"))
            .build()

        val manager = NotificationManagerCompat.from(context)
        manager.notify(AppConstants.MISSED_CALL_NOTIFICATION_ID, notification)
    }

    fun dismissNotification() {
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(AppConstants.CALL_NOTIFICATION_ID)
        stopVibration()
    }
    
    private fun startIncomingCallVibration() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED && 
            vibrator.hasVibrator()) {
            val pattern = longArrayOf(0, 800, 400, 800, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createWaveform(pattern, 0)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, 0)
            }
        }
    }
    
    private fun stopVibration() {
        if (vibrator.hasVibrator()) {
            vibrator.cancel()
        }
    }
}
