package com.example.spotifyplaylistdownloader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecyclerAdapterHistory(private var playlistHistoryList: ArrayList<PlaylistHistory>) : RecyclerView.Adapter<RecyclerAdapterHistory.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder{
        val holder = HistoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        )
        holder.itemView.setOnClickListener {
//            val songName = PlaylistHistoryList[holder.adapterPosition].songName
//            val artistName = songs[holder.adapterPosition].artistName
//            Toast.makeText(MainActivity.appContext, "Song - $songName by $artistName", Toast.LENGTH_SHORT).show()
        }
        return holder
    }

    override fun getItemCount(): Int {
        return playlistHistoryList.size
    }

    override fun onBindViewHolder(holder: RecyclerAdapterHistory.HistoryViewHolder, position: Int) {
        var playlist = playlistHistoryList[position]

        //display the values
        holder.playlistName.text = playlist.playlistName

        //set it to copy
        holder.copyButton.setOnClickListener {
            val urlText = playlist.playlistURL
            val clipboardManager = MainActivity.appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", urlText)
            clipboardManager.setPrimaryClip(clipData)

            Toast.makeText(MainActivity.appContext, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
        }

    }

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val playlistName = view.findViewById<TextView>(R.id.song_name_history)
        val copyButton = view.findViewById<ImageButton>(R.id.button_copy)
    }
}