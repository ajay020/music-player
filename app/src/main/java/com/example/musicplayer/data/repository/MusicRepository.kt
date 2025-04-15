package com.example.musicplayer.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Helper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch


@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playlistRepository: PlaylistRepository
) {
    private val allSongs = mutableListOf<Song>()

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATA // Path to the file
    )

    // Load all songs from the device
    suspend fun getAllSongs(): List<Song> {
        return withContext(Dispatchers.IO) {
            if (allSongs.isNotEmpty()) {
                allSongs // Return cached songs if available
            } else {
                val tempSongs = mutableListOf<Song>()
                context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    MediaStore.Audio.Media.TITLE
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val albumIdColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val durationColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn)
                        val artist = cursor.getString(artistColumn)
                        val albumName = cursor.getString(albumColumn)
                        val albumId = cursor.getLong(albumIdColumn).toString()
                        val duration = cursor.getLong(durationColumn)
                        val path = cursor.getString(pathColumn)

                        val contentUri = Helper.getAlbumArtUri(albumId.toLong())

                        val song = Song(
                            id = id,
                            uri = contentUri,
                            title = title,
                            artist = artist,
                            albumId = albumId,
                            duration = duration,
                            path = path
                        )
                        tempSongs.add(song)
                    }
                }
                allSongs.addAll(tempSongs) // Cache the loaded songs
                tempSongs
            }
        }
    }

    suspend fun getSongs(type: String, id: Long? = null, folderName: String? = null): List<Song> {
        return when (type) {
            "ARTIST" -> id?.let { getSongsByArtistId(it) } ?: emptyList()
            "ALBUM" -> id?.let { getSongsByAlbumId(it) } ?: emptyList()
            "PLAYLIST" -> id?.let { getSongsByPlaylistId(it) } ?: emptyList()
            "FOLDER" -> folderName?.let { getSongsByFolderName(it) } ?: emptyList()
            else -> getAllSongs()
        }
    }

    private suspend fun getSongsByPlaylistId(playlistId: Long): List<Song> {
        val songIds = playlistRepository.getPlaylistById(playlistId)?.songIds ?: emptyList()
        return getSongsByIds(songIds)
    }


    suspend fun getSongsByIds(songIds: List<Long>): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media._ID} IN (${songIds.joinToString()})"

        context.contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val title =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val albumId =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                val album =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val duration =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), albumId
                )

                val song = Song(
                    id = id,
                    uri = albumArtUri,
                    title = title,
                    artist = artist,
                    albumId = albumId.toString(),
                    duration = duration,
                    path = path
                )
                songs.add(song)
            }
        }
        return@withContext songs
    }

    suspend fun getSongsByArtistId(artistId: Long): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.ARTIST_ID} = ?"
        val selectionArgs =
            arrayOf(getArtistIdFromMediaStore(artistId).toString()) // Use MediaStore's Artist ID

        context.contentResolver.query(
            uri, projection, selection, selectionArgs, MediaStore.Audio.Media.TITLE
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val title =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val albumId =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                val album =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val duration =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), albumId
                )

                val song = Song(
                    id = id,
                    uri = albumArtUri,
                    title = title,
                    artist = artist,
                    albumId = albumId.toString(),
                    duration = duration,
                    path = path
                )
                songs.add(song)
            }
        }
        return@withContext songs
    }

    suspend fun getSongsByAlbumId(albumId: Long): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.ALBUM_ID} = ?"
        val selectionArgs = arrayOf(albumId.toString())

        context.contentResolver.query(
            uri, projection, selection, selectionArgs, MediaStore.Audio.Media.TRACK
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val title =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val albumId =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                val album =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val duration =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), albumId
                )

                val song = Song(
                    id = id,
                    uri = albumArtUri,
                    title = title,
                    artist = artist,
                    albumId = albumId.toString(),
                    duration = duration,
                    path = path
                )
                songs.add(song)
            }
        }
        return@withContext songs
    }

    suspend fun getSongsByFolderName(folderName: String): List<Song> {
        val songs = mutableListOf<Song>()

        if (allSongs.isEmpty()) {
            allSongs.addAll(getAllSongs())
        }

        allSongs.forEach { song ->
            if (song.path.contains(folderName)) {
                songs.add(song)
            }
        }

        return songs
    }

    // Helper function to get the MediaStore's internal artist ID
    private suspend fun getArtistIdFromMediaStore(artistId: Long): Long =
        withContext(Dispatchers.IO) {
            var mediaStoreArtistId = -1L
            val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Audio.Artists._ID, MediaStore.Audio.Artists.ARTIST)
            val selection = "${MediaStore.Audio.Artists._ID} = ?"
            val selectionArgs = arrayOf(artistId.toString())

            context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        mediaStoreArtistId =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID))
                    }
                }
            mediaStoreArtistId
        }
}