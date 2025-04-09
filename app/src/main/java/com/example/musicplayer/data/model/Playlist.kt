package com.example.musicplayer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.musicplayer.utils.Converters

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false,
    val playlistType: PlaylistType? = null,
    val songCount: Int = 0,
    val imageUri: String? = null,
    @TypeConverters(Converters::class) // To store List<Long>
    val songIds: List<Long> = emptyList()
) {
    enum class PlaylistType {
        FAVORITES,
        RECENTLY_ADDED,
        MOST_PLAYED
    }
}
