package com.example.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri

object Helper {
    fun getAlbumArt(songUri: Uri, context: Context): Bitmap? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, songUri)
            val art = retriever.embeddedPicture  // Get the image bytes
            return art?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return null
    }

}