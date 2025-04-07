package com.example.musicplayer.data.model
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val albumId: String,
    val duration: Long,
    val path: String
): Parcelable
