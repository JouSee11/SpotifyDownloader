package com.example.spotifyplaylistdownloader

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class PlaylistHistory(val playlistName: String, val playlistURL: String)

class MySharedPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MxPref", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun savePlaylistToHistory(key: String, playlistInfo: PlaylistHistory) {
        // get the previous list (append it to the previous list)
        val oldList = getPlaylistHistory(key)?.toMutableList()
        //check if it is not the last input
        if (oldList?.get(0) != playlistInfo) {
            val updatedList = oldList?.apply { add(0, playlistInfo) } ?: mutableListOf(playlistInfo)
            val json = gson.toJson(updatedList)
            sharedPreferences.edit().putString(key, json).apply()
        }
    }

    fun getPlaylistHistory(key: String): MutableList<PlaylistHistory>? {
        val json = sharedPreferences.getString(key, null)
        return  if (json != null) {
            gson.fromJson(json, object : TypeToken<List<PlaylistHistory>>() {}.type)
        } else {
            null
        }
    }
}