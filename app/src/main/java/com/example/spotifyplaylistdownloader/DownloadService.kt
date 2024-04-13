package com.example.spotifyplaylistdownloader

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.chaquo.python.PyObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class DownloadService: Service() {

    private lateinit var mContext: Context
    private var serviceCallback: ServiceCallback? = null

    private var downloadJob: Job? = null
    private val myScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val downloaded = mutableListOf<String>()

    override fun onBind(intent: Intent?): IBinder? {
        return DownloadBinder()
    }

    inner class DownloadBinder : Binder() {
        fun getService(): DownloadService {
            return this@DownloadService
        }
    }
    fun setServiceCallback(callback: ServiceCallback) {
        serviceCallback = callback
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val playlistNameString = intent?.getStringExtra("playlistName")
        mContext = applicationContext

        when(intent?.action) {
            Actions.START.toString() -> start(playlistNameString.toString())
            Actions.STOP.toString() -> stop()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(playlistNameString: String) {
        val notification = NotificationCompat.Builder(this, "downloading_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Downloading")
            .setContentText("Downloading songs...")
            .build()

        startForeground(1, notification)


        //download process
        val myFunDownload: PyObject? = myModule?.get("download")

        //start the download
        downloadJob = myScope.launch {
            for ((song, artist) in songArtistMap!!) {
                val resultDownload = withContext(Dispatchers.IO) { myFunDownload?.call(song, artist, downloadDirecotry) }
                downloaded.add(song)
                // make the song appear in the music player
                if (resultDownload.toString() != "") {
                    saveToExternalStorage(resultDownload.toString(), song.toString(), artist, playlistNameString, mContext)

                    Toast.makeText(mContext,"Downloaded: $song", Toast.LENGTH_SHORT).show()
                }

                serviceCallback?.onSongDownloaded(song)



                //check if it should be canceled
                if (!isActive) {
                    break
                }
            }
//            isDownloading = false
//            downloadButton.text = "Downloaded"
//            downloadButton.isEnabled = false
//            downloadButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
            //delete from the list that are already downloaded
            downloaded.forEach { songArtistMap.remove(it) }
            downloaded.clear()

            serviceCallback?.finishedDownload()


        }
    }


    private fun stop() {
        //cancel download
        downloadJob?.cancel()

        //delete from the list that are already downloaded
        downloaded.forEach { songArtistMap.remove(it) }
        downloaded.clear()
        stopSelf()
    }
    enum class Actions {
        START, STOP
    }
}


