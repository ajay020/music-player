package com.example.musicplayer.data.model

import android.net.Uri

data class Album(
    val id: Long,
    val name: String,
    val songCount: Int,
    val coverUri: Uri
)
