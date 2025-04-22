package com.example.musicplayer.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.adapter.SongAdapter
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.Searchable
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.model.Sortable
import com.example.musicplayer.viewmodel.MusicViewModel
import com.example.musicplayer.viewmodel.PlaylistViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

@AndroidEntryPoint
class SongsFragment : Fragment(), Searchable, Sortable {
    private val musicViewModel: MusicViewModel by activityViewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private var originalSongsList: List<Song> = emptyList()
    private var songsList = mutableListOf<Song>()
    private var userPlaylists = listOf<Playlist>()

    private var currentPlayingIndex = -1

    private var songAdapter: SongAdapter? = SongAdapter(
        songs = songsList,
        onSongClick = { song, index -> playSong(song, index) },
        onMoreOptionsClick = { showSongOptionsDialog(it) },
    )

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                musicViewModel.loadSongsBasedOnType("SONGS")
            } else {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

                if (!shouldShowRequestPermissionRationale(permission)) {
                    // User clicked "Don't ask again"
                    showGoToSettingsDialog()
                } else {
                    Log.e("song fragment", "Permission Denied")
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_songs, container, false)
        return view
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = songAdapter

        // Observe data and set up listeners here
        requestAudioPermission()
        observeUserPlaylists()
        observeSongs()
        observeCurrentSong()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        val permission = Manifest.permission.READ_MEDIA_AUDIO
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Load songs only if list is empty (to avoid duplicate loading)
            if (songsList.isEmpty()) {
                musicViewModel.loadSongsBasedOnType("SONGS")
            }
        }
    }

    override fun onSearchQuery(query: String?) {
        query?.let {
            val filteredList = if (it.isNotBlank()) {
                originalSongsList.filter { song ->
                    song.title.contains(it, ignoreCase = true) ||
                            song.artist.contains(it, ignoreCase = true)
                }
            } else {
                originalSongsList
            }
            updateRecyclerView(filteredList)
        }
    }

    private fun updateRecyclerView(filteredSongs: List<Song>) {
        songsList.clear()
        songsList.addAll(filteredSongs)
        songAdapter?.notifyDataSetChanged()
    }

    private fun showSongOptionsDialog(song: Song) {
        val builder = MaterialAlertDialogBuilder(requireContext())
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

        val builder =
            MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_MusicPlayer_Dialog)
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
        playlistViewModel.allPlaylists.observe(viewLifecycleOwner) { playlists ->
            userPlaylists = playlists.filter { !it.isDefault }
        }
    }

    private fun observeSongs() {
        musicViewModel.songs.observe(viewLifecycleOwner) { songs ->
            originalSongsList = songs // Ensure originalSongsList is updated here
            songsList.clear()
            songsList.addAll(songs)
            songAdapter?.notifyDataSetChanged()
        }
    }

    private fun observeCurrentSong() {
        musicViewModel.currentSong.observe(viewLifecycleOwner) { selectedSong ->

            val newIndex = songsList.indexOfFirst { it.id == selectedSong?.id }

            if (newIndex != -1) {
                val oldIndex = currentPlayingIndex
                currentPlayingIndex = newIndex

                songAdapter?.currentPlayingIndex = newIndex

                if (oldIndex != -1 && oldIndex != newIndex) {
                    songAdapter?.notifyItemChanged(oldIndex)
                }
                songAdapter?.notifyItemChanged(newIndex)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestAudioPermission() {
        val permission =
            Manifest.permission.READ_MEDIA_AUDIO

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Already granted
                musicViewModel.loadSongsBasedOnType("SONGS")
            }

            shouldShowRequestPermissionRationale(permission) -> {
                // User previously denied but didn't select "Don't ask again"
                showPermissionRationaleDialog()
            }

            else -> {
                //  First time or "Don't ask again" selected — try requesting anyway
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permission Needed")
            .setMessage("This app needs access to your audio files to show your songs.")
            .setPositiveButton("Allow") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showGoToSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Please enable Media permission from Settings to load your songs.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${requireContext().packageName}".toUri()
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun playSong(song: Song, index: Int) {
        musicViewModel.playSong(song, index)
    }

    override fun onSortBy(sortBy: String) {
        when (sortBy) {
            "Title" -> {
                val sortedList = originalSongsList.sortedBy { it.title }
                updateRecyclerView(sortedList)
            }

            "Artist" -> {
                val sortedList = originalSongsList.sortedBy { it.artist }
                updateRecyclerView(sortedList)
            }

            "Duration" -> {
                val sortedList =
                    originalSongsList.sortedBy { it.duration } // Assuming duration is a comparable property
                updateRecyclerView(sortedList)
            }
        }
    }

    override fun getSortOptions(): List<String> {
        return listOf("Title", "Artist", "Duration")
    }
}
