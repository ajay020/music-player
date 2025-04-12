package com.example.musicplayer.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.musicplayer.data.model.Artist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistsRepository @Inject constructor(private val context: Context) {

    suspend fun queryArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val artists = mutableListOf<Artist>()
        val uri: Uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )
        val sortOrder = MediaStore.Audio.Artists.ARTIST + " ASC"

        context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID))
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))
                val numberOfAlbums =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS))
                val numberOfTracks =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))

                val artist = Artist(id, name, numberOfAlbums, numberOfTracks)
                artists.add(artist)
            }
        }
        return@withContext artists
    }
}