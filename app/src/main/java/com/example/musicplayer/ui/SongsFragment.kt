package com.example.musicplayer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.adapter.SongAdapter
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.viewmodel.MusicViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SongsFragment : Fragment() {
    private val musicViewModel: MusicViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private var songsList = mutableListOf<Song>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                musicViewModel.loadSongs("SONGS")
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
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        songAdapter = SongAdapter(
            songs = songsList,
            onSongClick = { song, index  ->
                playSong(song, index)
            })
        recyclerView.adapter = songAdapter

        // Request READ_MEDIA_AUDIO permission
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            musicViewModel.loadSongs("SONGS")
        }

        musicViewModel.songs.observe(viewLifecycleOwner) { songs ->
            songsList.clear()  // Clear the old list before adding new songs
            songsList.addAll(songs)
            songAdapter.notifyDataSetChanged() // Notify adapter about the new data
        }

        return view
    }

    private fun playSong(song: Song, index: Int) {
        musicViewModel.playSong(song, index)
    }
}
