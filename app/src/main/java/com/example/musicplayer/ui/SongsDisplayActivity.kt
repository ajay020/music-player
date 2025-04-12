package com.example.musicplayer.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.adapter.SongAdapter
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.viewmodel.MusicViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SongsDisplayActivity : AppCompatActivity() {
    private val musicViewModel: MusicViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private var originalSongsList: List<Song> = emptyList() // Store the original list
    private var songsList = mutableListOf<Song>()
    private lateinit var titleTextView: TextView
    private lateinit var coverImageView: ImageView
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_songs_display)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.songs_display)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button in Toolbar
        supportActionBar?.title = ""

        titleTextView = findViewById(R.id.toolbar_title)
        coverImageView = findViewById(R.id.toolbar_image)
        searchView = findViewById(R.id.search_view)
        setupSearchView()

        // Initially set the title on the Toolbar
        titleTextView.text =
            intent.getStringExtra("PLAYLIST_NAME") ?: intent.getStringExtra("ARTIST_NAME")
                    ?: intent.getStringExtra("ALBUM_NAME") ?: "Songs"

        recyclerView = findViewById(R.id.songs_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter(
            songsList,
            onSongClick = { song, index ->
                playSong(song, index)
            },
            onBackButtonClick = { finish() },
        )
        recyclerView.adapter = songAdapter

        // Retrieve data from Intent and save to SavedStateHandle
        loadIntentDataIntoViewModel()
        observeSongs()
    }

    private fun loadIntentDataIntoViewModel() {
        val type = intent.getStringExtra("TYPE")
        intent.getLongExtra("PLAYLIST_ID", -1).takeIf { it != -1L }?.let {
            musicViewModel.savedStateHandle["PLAYLIST_ID"] = it
        }
        intent.getLongExtra("ARTIST_ID", -1).takeIf { it != -1L }?.let {
            musicViewModel.savedStateHandle["ARTIST_ID"] = it
        }
        intent.getLongExtra("ALBUM_ID", -1).takeIf { it != -1L }?.let {
            musicViewModel.savedStateHandle["ALBUM_ID"] = it
        }
        musicViewModel.savedStateHandle["TYPE"] = type
        // The ViewModel will now automatically use the data from SavedStateHandle
        // in its init block and loadSongsBasedOnType()

        updateToolbarInfo(type) // Update toolbar here
    }

    private fun updateToolbarInfo(type: String?) {
        when (type) {
            "PLAYLIST" -> {
                titleTextView.text =
                    intent.getStringExtra("PLAYLIST_NAME") ?: "Playlist Songs"
                // Load playlist image if you have a way to get it
                // Example (replace with your actual logic):
                val playlistId = intent.getLongExtra("PLAYLIST_ID", -1L)
                if (playlistId != -1L) {
                    loadPlaylistImage(playlistId)
                } else {
                    hideToolbarImage()
                }
            }

            "ARTIST" -> {
                titleTextView.text = intent.getStringExtra("ARTIST_NAME") ?: "Artist Songs"
                // Load artist image if available
                val artistId = intent.getLongExtra("ARTIST_ID", -1L)
                if (artistId != -1L) {
                    loadArtistImage(artistId)
                } else {
                    hideToolbarImage()
                }
            }

            "ALBUM" -> {
                titleTextView.text = intent.getStringExtra("ALBUM_NAME") ?: "Album Songs"
                // Load album image using album ID
                val albumId = intent.getLongExtra("ALBUM_ID", -1L)
                if (albumId != -1L) {
                    loadAlbumImage(albumId)
                } else {
                    hideToolbarImage()
                }
            }

            else -> {
                titleTextView.text = "Songs"
                hideToolbarImage()
            }
        }
    }

    private fun loadPlaylistImage(playlistId: Long) {
        // Implement logic to fetch playlist image URI based on playlistId
        // This might involve querying your local database or using a default image
        val imageUri = getPlaylistImageUri(playlistId) // Replace with your actual method
        if (imageUri != null) {
            showToolbarImage(imageUri)
        } else {
            hideToolbarImage()
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle submission (optional - you can perform search on text change)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSongs(newText)
                return true
            }
        })
    }

    private fun filterSongs(query: String?) {
        query?.let {
            val filteredList = if (it.isNotBlank()) {
                originalSongsList.filter { song -> // Filter the original list
                    song.title.contains(it, ignoreCase = true) || song.artist.contains(
                        it,
                        ignoreCase = true
                    )
                }
            } else {
                originalSongsList // If query is empty, show the original list
            }
            updateRecyclerView(filteredList)
        }
    }

    private fun updateRecyclerView(filteredSongs: List<Song>) {
        songsList.clear()
        songsList.addAll(filteredSongs)
        songAdapter.notifyDataSetChanged()
    }

    private fun loadArtistImage(artistId: Long) {
        // Implement logic to fetch artist image URI based on artistId
        // You might need to query the MediaStore or use a default image
        val imageUri = getArtistImageUri(artistId) // Replace with your actual method
        if (imageUri != null) {
            showToolbarImage(imageUri)
        } else {
            hideToolbarImage()
        }
    }

    private fun loadAlbumImage(albumId: Long) {
        // Use Glide to load album art from the MediaStore
        val albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")
        showToolbarImage(albumArtUri)
    }

    private fun showToolbarImage(uri: Uri) {
        coverImageView.visibility = android.view.View.VISIBLE
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_music_placeholder) // Default placeholder
            .error(R.drawable.ic_music_placeholder)     // Error placeholder
            .into(coverImageView)
    }

    private fun hideToolbarImage() {
        coverImageView.visibility = android.view.View.GONE
    }

    // Implement these methods based on how you store and access images
    private fun getPlaylistImageUri(playlistId: Long): Uri? {
        // Your implementation here
        return null
    }

    private fun getArtistImageUri(artistId: Long): Uri? {
        // Your implementation here
        return null
    }

    private fun observeSongs() {
        musicViewModel.songs.observe(this) { songs ->
            originalSongsList = songs // Store the original list
            songsList.clear()
            songsList.addAll(originalSongsList) // Initialize the displayed list
            songAdapter.notifyDataSetChanged()
        }
    }

    private fun playSong(song: Song, index: Int) {
        musicViewModel.playSong(song, index)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}