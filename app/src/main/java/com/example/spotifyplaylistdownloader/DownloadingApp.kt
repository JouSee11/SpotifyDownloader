package com.example.spotifyplaylistdownloader

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class DownloadingApp: Application() {

    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            "downloading_channel",
            "Downloading Notifications",
            NotificationManager.IMPORTANCE_HIGH
            )

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}