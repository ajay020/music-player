package com.example.musicplayer.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.adapter.SongAdapter
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.ui.player.PlayerActivity
import com.example.musicplayer.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlbumSongsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private val songList = mutableListOf<Song>()

    private val albumViewModel: AlbumViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_songs)

        val albumId = intent.getLongExtra("albumId", -1)
        val albumName = intent.getStringExtra("albumName") ?: "Unknown Album"
        val albumCoverUri = intent.getStringExtra("albumCoverUri") ?: ""

        recyclerView = findViewById(R.id.songs_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        songAdapter = SongAdapter(
            songList,
            albumCoverUri,
            albumName,
            onSongClick = {
                playSong(it)
            },
        )
        recyclerView.adapter = songAdapter

        albumViewModel.songs.observe(this) { songs ->
            songList.clear()
            songList.addAll(songs)
            songAdapter.notifyDataSetChanged()
        }

        albumViewModel.loadSongsByAlbum(albumId)
    }

//    private fun setAlbumCover(albumCoverUri: String) {
//        Log.d("AlbumSongsActivity", "Album Cover URI: ${albumCoverUri}")
//
//        Glide.with(this)
//            .load(albumCoverUri)
//            .placeholder(R.drawable.ic_music_placeholder)  // A default image in case of no album art
//            .error(R.drawable.ic_music_placeholder)  // If loading fails
//            .into(albumCover)
//    }

    private fun playSong(song: Song) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("playlistType", "ALBUM")
            putExtra("playlistId", song.albumId)
            putExtra("currentSongId", song.id)
        }

        startActivity(intent)
    }
}
