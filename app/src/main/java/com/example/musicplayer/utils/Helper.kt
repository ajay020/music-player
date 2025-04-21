package com.example.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import java.util.Locale
import java.util.concurrent.TimeUnit

object Helper {
    fun getEmbeddedAlbumArt(context: Context, songUri: Uri): Bitmap? {
        return try {
            val mmr = MediaMetadataRetriever()
            try {
                mmr.setDataSource(context, songUri) // instead of URI version
            } catch (e: Exception) {
                Log.e("MMR_ERROR", "setDataSource failed", e)
            }
            val artBytes = mmr.embeddedPicture
            mmr.release()
            artBytes?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun getAlbumArtUri(albumId: Long): Uri {
        return Uri.parse("content://media/external/audio/albumart/$albumId")
    }

    fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600 // Corrected calculation
        val minutes = (totalSeconds % 3600) / 60 // Corrected calculation
        val seconds = totalSeconds % 60

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        }
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

}