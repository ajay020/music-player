package com.example.musicplayer.ui.home

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import com.example.musicplayer.R
import com.example.musicplayer.adapter.SongAdapter
import com.example.musicplayer.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "HomeFragment"

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var adapter: SongAdapter
    private val viewModel: HomeViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("HomeFragment", "Permission Granted")
                viewModel.loadSongs()
            } else {
                Log.e("HomeFragment", "Permission Denied")
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request READ_MEDIA_AUDIO permission
//        if (ContextCompat.checkSelfPermission(requireContext(),
//                Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED
//            ) {
//            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
//        } else {
//            viewModel.loadSongs()
//        }
//
//        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSongs)
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())

//        adapter = SongAdapter { song ->
//            val intent = Intent(requireContext(), PlayerActivity::class.java)
//            intent.putExtra("SONG_PATH", song.path)
//            intent.putExtra("SONG_TITLE", song.title)
//            intent.putExtra("SONG_ARTIST", song.artist)
//
//            startActivity(intent)
//        }
//        recyclerView.adapter = adapter
//        viewModel.songs.observe(viewLifecycleOwner) { songs ->
//            adapter.submitList(songs)
//        }

//        viewModel.loadSongs() // Fetch songs from MediaStore
    }
}
