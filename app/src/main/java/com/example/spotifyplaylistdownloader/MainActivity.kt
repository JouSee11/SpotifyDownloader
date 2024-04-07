package com.example.spotifyplaylistdownloader

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ModuleInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException


lateinit var downloadDirecotry: String
var playlistName: String? = ""
var songArtistMap = mutableMapOf<String, String>()

lateinit var py: Python
lateinit var myModule: PyObject
lateinit var myFunNames: PyObject

class MainActivity : AppCompatActivity() {

    //private val songsAdapter = RecyclerAdapter()
    companion object {
        lateinit var appContext: Context
            private set
    }

    private var readPermissionGranted = false
    private var writePermissionGranted = true
    lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

    private val myScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    lateinit var songsAdapter: RecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        downloadDirecotry = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        appContext = this
        // widgets

        //initialize python
        if (! Python.isStarted()) { Python.start(AndroidPlatform(this)); }

        py = Python.getInstance()
        myModule= py.getModule("get_spotify_names")
        myFunNames = myModule["get_names"]!!
    }


}
//        //widgets
//
//        val pasteButton = findViewById<Button>(R.id.pasteButton)
//        val downloadButton = findViewById<Button>(R.id.button_download)
//        val editText = findViewById<EditText>(R.id.editText)
//        val playlistText = findViewById<TextView>(R.id.playlist_name)
//
//        //recycler view
//        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        // "context" must be an Activity, Service or Application object from your app.
//        if (! Python.isStarted()) {
//            Python.start(AndroidPlatform(this));
//        }
//
//        // load the python file and the function
//        val py = Python.getInstance()
//        val myModule: PyObject? = py.getModule("get_spotify_names")
//        val myFunNames: PyObject? = myModule?.get("get_names")
//        val myFunDownload: PyObject? = myModule?.get("download")
//
//        //ask permissions
//
//
//        fun createSongList(playlistLink: String): ArrayList<Song>{
//            //get playlist songs and add to recycler view
//            val artistSong = myFunNames?.call(playlistLink, "songs")?.asMap()!!.map { (key, value) -> key.toString() to value.asList() }.toMap()
//            val list = ArrayList<Song>()
//
//            songArtistMap = artistSong?.map { (key, value) -> key to value.toString().substringBefore(",").substringAfter("[")}!!.toMap().toMutableMap()
//            println(songArtistMap)
//
//            //add the output to song type and to list
//            for ((key, value) in artistSong!!){
//                val valueList = value.toList()
//                var artist = valueList[0].toString()
//                artist = if (artist.length > 20) "${artist.substring(0, 20)}..." else artist
//                list.add(Song(songName = key.toString(), artistName =  artist, duration =  valueList[1].toString(), state = 0 ))
//                //songArtistMap += key.toString() to valueList[0].toString()
//            }
//            return list
//
//        }
//
//        //paste songs to ui
//        pasteButton.setOnClickListener {
//            //check if the user is connected
//            val connected = checkInternetConnectivity()
//            if (!connected) {
//                AlertDialog.Builder(this)
//                    .setTitle("Not connected")
//                    .setMessage("Please connect to the internet to proceed")
//                    .setPositiveButton("Ok") {_, _ -> }
//                    .show()
//            }
//            else {
//                myScope.launch {
//                    //get playlist link form the bar
//                    val playlistLink = editText.text.toString()
//                    // get the playlist name
//                    playlistName = myFunNames?.call(playlistLink, "pl_name")?.toString()
//                    playlistText.text = playlistName
//                    downloadButton.isEnabled = true
//                    //get playlist songs and add to recycler view
//                    val songData = createSongList(playlistLink)
//                    songsAdapter = RecyclerAdapter(songData)
//                    recyclerView.adapter = songsAdapter
//                }
//            }
//        }
//
//        //variables for the download
//        var isDownloading = false
//        var downloadJob: Job? = null // Store reference to the download job
//        val downloaded = mutableListOf<String>()
//
//        downloadButton.setOnClickListener {
//            if (!isDownloading) {
//                //update the ui button
//                isDownloading = true
//                downloadButton.text = "Cancel download"
//                downloadButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
//
//                //start the download
//                downloadJob = myScope.launch {
//                    songsAdapter.allStartDownload()
//                    for ((song, artist) in songArtistMap!!) {
//                        val resultDownload = withContext(Dispatchers.IO) { myFunDownload?.call(song, artist, downloadDirecotry) }
//                        downloaded.add(song)
//                        // make the song appear in the music player
//                        if (resultDownload.toString() != "") {
//                            saveToExternalStorage(resultDownload.toString(), song.toString(), artist, playlistName.toString(), this@MainActivity)
//
//                            Toast.makeText(this@MainActivity,"Downloaded: $song", Toast.LENGTH_SHORT).show()
//                        }
//                        songsAdapter.allOneFinished(song)
//
//
//                        //check if it should be canceled
//                        if (!isActive) {
//                            break
//                        }
//                    }
//                    isDownloading = false
//                    downloadButton.text = "Download"
//                    downloadButton.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.green))
//                    //delete from the list that are already downloaded
//                    downloaded.forEach { songArtistMap.remove(it) }
//                    downloaded.clear()
//                }
//            } else {
//                //cancel download
//                downloadJob?.cancel()
//                isDownloading = false
//                downloadButton.text = "Download"
//                downloadButton.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.green))
//                songsAdapter.cancelAllDownload()
//
//                //delete from the list that are already downloaded
//                downloaded.forEach { songArtistMap.remove(it) }
//                downloaded.clear()
//            }
//        }
//
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        myScope.cancel()
//    }
//
//    private fun updateOrRequestPermissions() {
//        val hasReadPermission = ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.READ_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED
//        val hasWritePermission = ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED
//        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
//
//        readPermissionGranted = hasReadPermission
//        writePermissionGranted = hasWritePermission || minSdk29
//
//        val permissionsToRequest = mutableListOf<String>()
//        if (!readPermissionGranted) {
//            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
//        }
//        if (!writePermissionGranted) {
//            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        }
//        if (permissionsToRequest.isNotEmpty()) {
//            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
//        }
//
//
//    }
//
//    private fun checkInternetConnectivity(): Boolean {
//        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connectivityManager.activeNetwork
//        val capabilities = connectivityManager.getNetworkCapabilities(network)
//
//        //device is connected
//        return capabilities != null //&& capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
//    }
//}
//
suspend fun singleDownload(artist: String, song: String) {
//

    val myFunDownload: PyObject? = myModule?.get("download")

    val resultDownload = withContext(Dispatchers.IO) { myFunDownload?.call(song, artist, downloadDirecotry)}
    Log.println(Log.INFO, "download", "downloaded")
    saveToExternalStorage(resultDownload.toString(), song, artist, playlistName.toString(), MainActivity.appContext)
    Log.println(Log.INFO, "download", "added to media stor")

}

fun saveToExternalStorage(mp3Path: String, title: String, artist:String, album: String, context: Context): Boolean {
    val contentResolver = context.contentResolver
    val audioCollection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

    val contentValues = ContentValues().apply {
        put(MediaStore.Audio.Media.DISPLAY_NAME, title)
        put(MediaStore.Audio.Media.ARTIST, artist)
        put(MediaStore.Audio.Media.ALBUM, album)
        put(MediaStore.Audio.Media.RELATIVE_PATH, "Music")
        //put(MediaStore.Audio.Media.RELATIVE_PATH, mp3Path)
        put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
    }
    return try {
        contentResolver.insert(audioCollection, contentValues)?.also { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(File(mp3Path)).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }

}


fun checkInternetConnectivity(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)

    //device is connected
    return capabilities != null //&& capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}

//fun updateOrRequestPermissions() {
//    val hasReadPermission = ContextCompat.checkSelfPermission(
//        this,
//        Manifest.permission.READ_EXTERNAL_STORAGE
//    ) == PackageManager.PERMISSION_GRANTED
//    val hasWritePermission = ContextCompat.checkSelfPermission(
//        this,
//        Manifest.permission.WRITE_EXTERNAL_STORAGE
//    ) == PackageManager.PERMISSION_GRANTED
//    val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
//
//    readPermissionGranted = hasReadPermission
//    writePermissionGranted = hasWritePermission || minSdk29
//
//    val permissionsToRequest = mutableListOf<String>()
//    if (!readPermissionGranted) {
//        permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
//    }
//    if (!writePermissionGranted) {
//        permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//    }
//    if (permissionsToRequest.isNotEmpty()) {
//        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
//    }
//
//
//}