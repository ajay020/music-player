package com.example.musicplayer.data.repository

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.musicplayer.data.model.Song
import javax.inject.Inject

class SongRepository @Inject constructor(private val context: Context) {

    fun getSongs(): List<Song> {
        val songList = mutableListOf<Song>()
        val contentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATE_ADDED,
        )

        val cursor = contentResolver.query(uri, projection, null, null, null)
        Log.d("SongRepository", "Cursor Count: ${cursor?.count}")

        cursor?.use {
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

            while (it.moveToNext()) {
                val path = it.getString(pathColumn)
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)

                songList.add(Song(title, artist, path))
            }
        }
        return songList
    }
}
