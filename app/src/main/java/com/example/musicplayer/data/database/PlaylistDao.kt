package com.example.musicplayer.data.database

import androidx.room.*
import com.example.musicplayer.data.model.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): Playlist?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: Playlist): Long // Returns the row ID of the inserted item


    @Query("SELECT * FROM playlists WHERE name = :playlistName") // New query
    suspend fun getPlaylistByName(playlistName: String): Playlist?

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("SELECT * FROM playlists WHERE isDefault = 1 ORDER BY playlistType ASC")
    fun getDefaultPlaylists(): Flow<List<Playlist>>
}