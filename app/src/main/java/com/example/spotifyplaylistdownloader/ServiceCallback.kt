package com.example.spotifyplaylistdownloader

interface ServiceCallback {
    fun onSongDownloaded(song: String)

    fun finishedDownload()
}