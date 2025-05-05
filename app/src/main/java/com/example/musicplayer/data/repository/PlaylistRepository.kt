package com.example.musicplayer.data.repository

import com.example.musicplayer.data.database.PlaylistDao
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
) {

    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun getPlaylistById(playlistId: Long): Playlist? =
        playlistDao.getPlaylistById(playlistId)

    suspend fun insertPlaylist(playlist: Playlist): Long = playlistDao.insertPlaylist(playlist)

    suspend fun updatePlaylist(playlist: Playlist) = playlistDao.updatePlaylist(playlist)

    suspend fun deletePlaylist(playlist: Playlist) = playlistDao.deletePlaylist(playlist)

    fun getDefaultPlaylists(): Flow<List<Playlist>> = playlistDao.getDefaultPlaylists()

    // Function to add a song ID to a playlist
    suspend fun addSongIdToPlaylist(playlistId: Long, songId: Long) {
        val playlist = getPlaylistById(playlistId)

        playlist?.let {
            val updatedSongIds = it.songIds.toMutableSet()
            updatedSongIds.add(songId)
            val updatedPlaylist =
                it.copy(songIds = updatedSongIds.toList(), songCount = updatedSongIds.size)
            updatePlaylist(updatedPlaylist)
        }
    }

    // Function to remove a song ID from a playlist
    suspend fun removeSongIdFromPlaylist(playlistId: Long, songId: Long) {
        val playlist = getPlaylistById(playlistId)

        playlist?.let {
            val updatedSongIds = it.songIds.toMutableSet()
            updatedSongIds.remove(songId)
            val updatedPlaylist =
                it.copy(songIds = updatedSongIds.toList(), songCount = updatedSongIds.size)
            updatePlaylist(updatedPlaylist)
        }
    }
}