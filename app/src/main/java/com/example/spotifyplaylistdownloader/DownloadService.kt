package com.example.spotifyplaylistdownloader

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager

class DownloadService : Service() {



    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start foreground service
        startForeground(1, Notification())

        // Acquire wakelock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.example.your-package:DownloadService")
        wakeLock?.acquire(10*60*1000L /*10 minutes*/)

        // Start download task
        startDownload()

        return START_STICKY
    }

    private fun startDownload() {
        // Your download logic goes here
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(screenReceiver)
        // Release wakelock
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

class ScreenReceiver : BroadcastReceiver() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_OFF) {
            // Screen turned off, acquire wakelock
            val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.example.your-package:DownloadServiceWakeLock")
            wakeLock?.acquire(10*60*1000L /*10 minutes*/)
        } else if (intent?.action == Intent.ACTION_SCREEN_ON) {
            // Screen turned on, release wakelock
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        }
    }
}

// Register the BroadcastReceiver in your activity or service
val screenReceiver = ScreenReceiver()
val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)


