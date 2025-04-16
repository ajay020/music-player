package com.example.musicplayer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.MainActivity
import com.example.musicplayer.R
import com.example.musicplayer.adapter.SongAdapter
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.Searchable
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.viewmodel.MusicViewModel
import com.example.musicplayer.viewmodel.PlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SongsFragment : Fragment(), Searchable {
    private val musicViewModel: MusicViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private var originalSongsList: List<Song> = emptyList() // Store the original list
    private var songsList = mutableListOf<Song>()
    private var userPlaylists = listOf<Playlist>()

    private var songAdapter: SongAdapter? = SongAdapter(
        songs = songsList,
        onSongClick = { song, index -> playSong(song, index) },
        onBackButtonClick = { (context as? MainActivity)?.finish() },
        onSearchButtonClick = { Log.d("SongsFragment", "Search button in header clicked") },
        onMoreOptionsClick = { showSongOptionsDialog(it) }
    )

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                musicViewModel.loadSongsBasedOnType("SONGS")
            } else {
                Log.e("song fragment", "Permission Denied")
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_songs, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = songAdapter

        // Observe data and set up listeners here
        requestAudioPermission()
        observeUserPlaylists()
        observeSongs()

        Log.d("SongsFragment", "onViewCreated: originalSongsList size = ${originalSongsList.size}")
    }

    override fun onSearchQuery(query: String?) {
        Log.d(
            "SongsFragment",
            "onSearchQuery called with query: $query, originalSongsList size = ${originalSongsList.size}"
        )
        query?.let {
            val filteredList = if (it.isNotBlank()) {
                originalSongsList.filter { song ->
                    song.title.contains(it, ignoreCase = true) ||
                            song.artist.contains(it, ignoreCase = true)
                }
            } else {
                originalSongsList
            }
            Log.d("SongsFragment", "onSearchQuery: filteredList size = ${filteredList.size}")
            updateRecyclerView(filteredList)
        }
    }

    private fun updateRecyclerView(filteredSongs: List<Song>) {
        songsList.clear()
        songsList.addAll(filteredSongs)
        songAdapter?.notifyDataSetChanged()
    }

    private fun showSongOptionsDialog(song: Song) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(song.title)
        val options = arrayOf("Add to Playlist", "Add to Queue", "Edit Tags") // Add more options
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> showAddToPlaylistDialog(song)
                1 -> {
                    // Implement Add to Queue logic
                    Toast.makeText(requireContext(), "Add to Queue clicked", Toast.LENGTH_SHORT)
                        .show()
                }

                2 -> {
                    // Implement Edit Tags logic
                    Toast.makeText(requireContext(), "Edit Tags clicked", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showAddToPlaylistDialog(song: Song) {
        if (userPlaylists.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "No playlists available. Create one first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val playlistNames = userPlaylists.map { it.name }.toTypedArray()
        val checkedItems =
            BooleanArray(userPlaylists.size) { false } // Initially no playlist selected

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add to Playlist")
        builder.setMultiChoiceItems(playlistNames, checkedItems) { _, which, isChecked ->
            if (isChecked) {
                val selectedPlaylist = userPlaylists[which]
                playlistViewModel.addSongToPlaylist(selectedPlaylist.id, song.id)
                Toast.makeText(
                    requireContext(),
                    "Added to ${selectedPlaylist.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        builder.setPositiveButton("Done") { dialog, _ ->
            // The adding to playlist happens in the setMultiChoiceItems listener
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun observeUserPlaylists() {
        // Observe user playlists
        playlistViewModel.allPlaylists.observe(viewLifecycleOwner) { playlists ->
            userPlaylists = playlists.filter { !it.isDefault }
        }
    }

    private fun observeSongs() {
        musicViewModel.songs.observe(viewLifecycleOwner) { songs ->
            Log.d("SongsFragment", "observeSongs: Received ${songs.size} songs")
            originalSongsList = songs // Ensure originalSongsList is updated here
            songsList.clear()
            songsList.addAll(songs)
            songAdapter?.notifyDataSetChanged()
            Log.d(
                "SongsFragment",
                "observeSongs: originalSongsList updated to size = ${originalSongsList.size}"
            )
        }
    }

    private fun requestAudioPermission() {
        // Request READ_MEDIA_AUDIO permission
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            musicViewModel.loadSongsBasedOnType("SONGS")
        }
    }

    private fun playSong(song: Song, index: Int) {
        musicViewModel.playSong(song, index)
    }
}
