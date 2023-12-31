package com.toochi.job_swift.backend

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.toochi.job_swift.MainActivity
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.AuthenticationManager.createNotifications
import com.toochi.job_swift.backend.AuthenticationManager.updateUserDetails
import com.toochi.job_swift.util.Constants.Companion.CHANNEL_ID
import com.toochi.job_swift.util.Constants.Companion.CHANNEL_NAME
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import kotlin.random.Random

class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val profileId = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .getString("profile_id", "")

        profileId?.let {
            updateUserDetails(it, hashMapOf("token" to token)) { _, _ -> }
        }

    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
            val notification = createNotification(message, pendingIntent)
            notificationManager.notify(notificationId, notification)
        } else {
            val notification = createLegacyNotification(message, pendingIntent)
            notificationManager.notify(notificationId, notification)
        }

        message.let {
            com.toochi.job_swift.model.Notification(
                title = it.notification?.title ?: "",
                body = it.notification?.body ?: "",
                userId = it.data["userId"] ?: "",
                ownerId = it.data["ownerId"] ?: "",
                jobId = it.data["jobId"] ?: ""
            ).also { notification ->
                createNotifications(notification) { _, _ -> }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE_HIGH).apply {
            name = "Job Application Notifications"
            description = "Notifications for new job applications"
            enableLights(true)
            lightColor = ContextCompat.getColor(this@FirebaseService, R.color.shade_green)
        }.also {
            notificationManager.createNotificationChannel(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(
        message: RemoteMessage,
        pendingIntent: PendingIntent
    ): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(message.notification?.title)
            .setContentText(message.notification?.body)
            .setSmallIcon(R.drawable.app_logo2)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createLegacyNotification(
        message: RemoteMessage,
        pendingIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.notification?.title)
            .setContentText(message.notification?.body)
            .setSmallIcon(R.drawable.app_logo2)
            .setAutoCancel(true)
            .setPriority(PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()
    }

}