package com.example.musicplayer.data.repository

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.musicplayer.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val context: Context
) {
    // Cache for songs by category
    private val allSongs = mutableListOf<Song>()
    private val albumSongsMap = mutableMapOf<String, List<Song>>()
    private val artistSongsMap = mutableMapOf<String, List<Song>>()
    private val playlistSongsMap = mutableMapOf<String, List<Song>>()

    // Load all songs from the device
    suspend fun loadSongs(): List<Song> {
        return withContext(Dispatchers.IO) {
            // Clear cached data
            allSongs.clear()
            albumSongsMap.clear()
            artistSongsMap.clear()

            // Query MediaStore for songs on the device
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
            )

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
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val album = cursor.getString(albumColumn)
                    val albumId = cursor.getLong(albumIdColumn).toString()
                    val duration = cursor.getLong(durationColumn)
                    val path = cursor.getString(pathColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val song = Song(
                        id = id,
                        uri = contentUri,
                        title = title,
                        artist = artist,
                        albumId = albumId,
                        duration = duration,
                        path = path
                    )
                    allSongs.add(song)

                    // Organize songs by album
                    val albumSongs =
                        albumSongsMap.getOrDefault(albumId, emptyList()).toMutableList()
                    albumSongs.add(song)
                    albumSongsMap[albumId] = albumSongs

                    // Organize songs by artist
                    val artistSongs =
                        artistSongsMap.getOrDefault(artist, emptyList()).toMutableList()
                    artistSongs.add(song)
                    artistSongsMap[artist] = artistSongs
                }
            }

            allSongs
        }
    }

    // Get all songs
    fun getAllSongs(): List<Song> = allSongs

    // Get songs by album ID
    fun getSongsByAlbum(albumId: String): List<Song> = albumSongsMap[albumId] ?: emptyList()

    // Get songs by artist name
    fun getSongsByArtist(artistName: String): List<Song> = artistSongsMap[artistName] ?: emptyList()

    // Get songs by playlist ID (would connect to a database or content provider for playlists)
    fun getSongsByPlaylist(playlistId: String): List<Song> =
        playlistSongsMap[playlistId] ?: emptyList()

    // Get the next song in a playlist
    fun getNextSong(currentSong: Song, playlistType: String, playlistId: String? = null): Song? {
        val playlist = when (playlistType) {
            "SONGS" -> allSongs
            "ALBUM" -> albumSongsMap[playlistId] ?: emptyList()
            "ARTIST" -> artistSongsMap[playlistId] ?: emptyList()
            "PLAYLIST" -> playlistSongsMap[playlistId] ?: emptyList()
            else -> emptyList()
        }

        if (playlist.isEmpty()) return null

        val currentIndex = playlist.indexOfFirst { it.id == currentSong.id }
        if (currentIndex == -1) return null

        // Get next song with wrap-around
        val nextIndex = (currentIndex + 1) % playlist.size
        return playlist[nextIndex]
    }

    // Get the previous song in a playlist
    fun getPreviousSong(
        currentSong: Song,
        playlistType: String,
        playlistId: String? = null
    ): Song? {
        val playlist = when (playlistType) {
            "SONGS" -> allSongs
            "ALBUM" -> albumSongsMap[playlistId] ?: emptyList()
            "ARTIST" -> artistSongsMap[playlistId] ?: emptyList()
            "PLAYLIST" -> playlistSongsMap[playlistId] ?: emptyList()
            else -> emptyList()
        }

        if (playlist.isEmpty()) return null

        val currentIndex = playlist.indexOfFirst { it.id == currentSong.id }
        if (currentIndex == -1) return null

        // Get previous song with wrap-around
        val previousIndex = if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
        return playlist[previousIndex]
    }
}
