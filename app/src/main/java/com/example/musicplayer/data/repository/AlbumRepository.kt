package com.example.musicplayer.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.musicplayer.data.model.Album
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Helper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumRepository @Inject constructor(private val context: Context) {
    suspend fun loadAlbums(): List<Album> {
        return withContext(Dispatchers.IO) {
            val albums = mutableListOf<Album>()

            val projection = arrayOf(
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = MediaStore.Audio.Media.ALBUM + " ASC"

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val albumNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

                val albumMap = mutableMapOf<Long, Album>()

                while (cursor.moveToNext()) {
                    val albumId = cursor.getLong(albumIdColumn)
                    val albumName = cursor.getString(albumNameColumn) ?: "Unknown Album"

                    val uri  = Helper.getAlbumArtUri(albumId)

                    val count = albumMap[albumId]?.songCount ?: 0
                    albumMap[albumId] = Album(albumId, albumName, count + 1, uri)
                }

                albums.addAll(albumMap.values.toList())
            }

            albums
        }
    }



    suspend fun loadSongsByAlbum(albumId: Long): List<Song> {
        return withContext(Dispatchers.IO) {
            val songs = mutableListOf<Song>()

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
            )

            val selection = "${MediaStore.Audio.Media.ALBUM_ID} = ?"
            val selectionArgs = arrayOf(albumId.toString())

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Media.TITLE
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Title"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val duration = cursor.getLong(durationColumn)
                    val path = cursor.getString(dataColumn)

                    val contentUri = Helper.getAlbumArtUri(albumId)

                    songs.add(
                        Song(
                            id,
                            contentUri,
                            title,
                            artist,
                            albumId.toString(),
                            duration,
                            path
                        )
                    )
                }
            }
            songs
        }
    }
}