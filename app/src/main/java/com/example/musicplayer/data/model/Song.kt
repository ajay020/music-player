package com.example.musicplayer.data.model
import android.net.Uri

data class Song(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val albumId: String,
    val duration: Long,
    val path: String
)
