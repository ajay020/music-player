package com.example.musicplayer.ui

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.adapter.SongAdapter
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.viewmodel.MusicViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class AlbumSongsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private val songList = mutableListOf<Song>()

    private val musicViewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_songs)

        // Get album details from intent
        val albumId = intent.getLongExtra("albumId", -1)
        val albumName = intent.getStringExtra("albumName") ?: "Unknown Album"
        val albumCoverUri = intent.getStringExtra("albumCoverUri") ?: ""

        recyclerView = findViewById(R.id.songs_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        songAdapter = SongAdapter(
            songList,
            albumCoverUri,
            albumName,
            onSongClick = { song, index ->
                playSong(song, index)
            },
            onBackButtonClick = { finish() },
            onSearchButtonClick = {

            }
        )
        recyclerView.adapter = songAdapter

        musicViewModel.songs.observe(this) { songs ->
            songList.clear()
            songList.addAll(songs)
            songAdapter.notifyDataSetChanged()
        }

        musicViewModel.loadSongs("ALBUM", albumId.toString())
    }

    private fun playSong(song: Song, index: Int) {
        musicViewModel.playSong(song, index)
    }
}
