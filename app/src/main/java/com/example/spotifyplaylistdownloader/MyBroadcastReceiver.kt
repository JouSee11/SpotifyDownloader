package com.example.spotifyplaylistdownloader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "ACTION_BUTTON_CLICKED") {

            Toast.makeText(context, "Download Stopped", Toast.LENGTH_SHORT).show()

            val myIntent = Intent(context, DownloadService::class.java).apply {
                action = DownloadService.Actions.STOP.toString()
            }
            val myContext = context as Context
            ContextCompat.startForegroundService(myContext, myIntent)

            //update ui



        }
    }
}