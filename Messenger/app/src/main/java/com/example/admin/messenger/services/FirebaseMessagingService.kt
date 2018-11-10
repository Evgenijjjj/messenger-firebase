package com.example.admin.messenger.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Build
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import com.example.admin.messenger.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService: FirebaseMessagingService() {
    private val notificationChannelID = "MessengerChannel"

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)

        if (p0?.data != null)
            sendNotification(p0)
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val data: Map<String,String> = remoteMessage.data

        val title = data["title"]
        val content = data["body"]

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel: NotificationChannel = NotificationChannel(notificationChannelID, "EDMT Notification", NotificationManager.IMPORTANCE_HIGH)

            notificationChannel.description = "test chanel for app test"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(this)

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.app_icon)
                .setTicker("Hearty 365")
                .setContentTitle(title)
                .setContentText(content)
                .setContentInfo("info")

        notificationManager.notify(1,notificationBuilder.build())
    }


}