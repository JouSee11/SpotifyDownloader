package com.example.spotifyplaylistdownloader

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.PyObject
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import java.io.Serializable


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlaylistFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val myScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var songsAdapter: RecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)

        //get link from the previous fragment
        val playlistLink = arguments?.getString("link").toString()
        val playlistNameString = myFunNames.call(playlistLink, "pl_name").toString()

        //widgets
        val imageView = view.findViewById<ImageView>(R.id.playlistImageView)
        val imageViewWide = view.findViewById<ImageView>(R.id.playlistImageViewWide)
        val playlistTextView = view.findViewById<TextView>(R.id.playlistName)
        val downloadButton = view.findViewById<Button>(R.id.downloadButton)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbarPlaylist)

        //toolbar
        (activity as AppCompatActivity).apply {
            setSupportActionBar(view.findViewById(R.id.toolbarPlaylist))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        toolbar.title = playlistNameString
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        //recycler view
        fun createSongList(playlistLink: String): ArrayList<Song>{
            //get playlist songs and add to recycler view
            val artistSong = myFunNames?.call(playlistLink, "songs")?.asMap()!!.map { (key, value) -> key.toString() to value.asList() }.toMap()
            val list = ArrayList<Song>()

            songArtistMap = artistSong?.map { (key, value) -> key to value.toString().substringBefore(",").substringAfter("[")}!!.toMap().toMutableMap()
            println(songArtistMap)

            //add the output to song type and to list
            for ((key, value) in artistSong!!){
                val valueList = value.toList()
                var artist = valueList[0].toString()
                artist = if (artist.length > 35) "${artist.substring(0, 35)}..." else artist
                list.add(Song(songName = key.toString(), artistName =  artist, duration =  valueList[1].toString(), state = 0 ))
                //songArtistMap += key.toString() to valueList[0].toString()
            }
            return list

        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        //get playlist songs and add to recycler view
        val songData = createSongList(playlistLink)
        songsAdapter = RecyclerAdapter(songData)
        recyclerView.adapter = songsAdapter


        //set playlist name
        playlistTextView.text = playlistNameString

        // handle images loads
        myScope.launch {
            //set the image to playlist thumbnail
            val imageUrl = myFunNames.call(playlistLink, "thumbnail").toString()
            //target to set the image to both normal and wide at once
            val targetImages = object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    // Set the bitmap to both ImageViews
                    imageView.setImageBitmap(bitmap)
                    imageViewWide.setImageBitmap(bitmap)

                    imageView.animate().alpha(1f).setDuration(500).start()
                    imageViewWide.animate().alpha(0.5f).setDuration(2000).start()
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    // Handle bitmap loading failure
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    // Handle bitmap loading preparation
                }
            }
            Picasso.get()
                .load(imageUrl)
                .into(targetImages)
        }

        //download all button
        val myFunDownload: PyObject? = myModule?.get("download")
        //variables for the download
        var isDownloading = false
        var downloadJob: Job? = null // Store reference to the download job
        val downloaded = mutableListOf<String>()

        downloadButton.setOnClickListener {

            if (!isDownloading) {
                //update the ui button
                isDownloading = true
                downloadButton.text = "Cancel download"
                downloadButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))

                //start the download
                downloadJob = myScope.launch {
                    songsAdapter.allStartDownload()
                    for ((song, artist) in songArtistMap!!) {
                        val resultDownload = withContext(Dispatchers.IO) { myFunDownload?.call(song, artist, downloadDirecotry) }
                        downloaded.add(song)
                        // make the song appear in the music player
                        if (resultDownload.toString() != "") {
                            saveToExternalStorage(resultDownload.toString(), song.toString(), artist, playlistNameString, requireContext())

                            Toast.makeText(requireContext(),"Downloaded: $song", Toast.LENGTH_SHORT).show()
                        }
                        songsAdapter.allOneFinished(song)


                        //check if it should be canceled
                        if (!isActive) {
                            break
                        }
                    }
                    isDownloading = false
                    downloadButton.text = "Downloaded"
                    downloadButton.isEnabled = false
                    downloadButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
                    //delete from the list that are already downloaded
                    downloaded.forEach { songArtistMap.remove(it) }
                    downloaded.clear()
                }
            } else {
                //cancel download
                downloadJob?.cancel()
                isDownloading = false
                downloadButton.text = "Download"
                downloadButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
                songsAdapter.cancelAllDownload()

                //delete from the list that are already downloaded
                downloaded.forEach { songArtistMap.remove(it) }
                downloaded.clear()
            }
        }


    fun onDestroy() {
        super.onDestroy()
        myScope.cancel()
    }




        return view
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Add your action here
                // For example, you can navigate back
                findNavController().navigateUp()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlaylistFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlaylistFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}