package com.example.musicplayer.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.utils.Converters

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Playlist::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope,
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_database"
                )
                    .addCallback(
                        PlaylistDatabaseCallback(scope)
                    ) // Pass the provider
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    class PlaylistDatabaseCallback @Inject constructor(
        private val scope: CoroutineScope,
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDefaultPlaylists(database.playlistDao())
                }
            }
        }

        private suspend fun populateDefaultPlaylists(playlistDao: PlaylistDao) {
            Log.d("PlaylistDatabaseCallback", "Creating default playlists...")

            if (playlistDao.getPlaylistByName("Favorites") == null) {
                Log.d("PlaylistDatabaseCallback", "Created")
                playlistDao.insertPlaylist(
                    Playlist(
                        name = "Favorites",
                        isDefault = true,
                        playlistType = Playlist.PlaylistType.FAVORITES
                    )
                )
            }
            if (playlistDao.getPlaylistByName("Recently Added") == null) {
                playlistDao.insertPlaylist(
                    Playlist(
                        name = "Recently Added",
                        isDefault = true,
                        playlistType = Playlist.PlaylistType.RECENTLY_ADDED
                    )
                )
            }
            if (playlistDao.getPlaylistByName("Most Played") == null) {
                playlistDao.insertPlaylist(
                    Playlist(
                        name = "Most Played",
                        isDefault = true,
                        playlistType = Playlist.PlaylistType.MOST_PLAYED
                    )
                )
            }
        }
    }
}
