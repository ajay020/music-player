package com.example.musicplayer.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.adapter.AlbumAdapter
import com.example.musicplayer.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.musicplayer.adapter.ArtistAdapter

import com.example.musicplayer.data.model.Artist
import com.example.musicplayer.data.model.Searchable
import com.example.musicplayer.data.model.Sortable
import com.example.musicplayer.viewmodel.ArtistsViewModel

@AndroidEntryPoint
class ArtistsFragment : Fragment(), Searchable, Sortable {
    private val artistsViewModel: ArtistsViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var artistAdapter: ArtistAdapter
    private var artistsList = mutableListOf<Artist>()
    private var originalArtistList = emptyList<Artist>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_artists, container, false)
        recyclerView = view.findViewById(R.id.artists_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        artistAdapter = ArtistAdapter(artistsList) { artist ->
            val intent = Intent(requireContext(), SongsDisplayActivity::class.java).apply {
                putExtra("TYPE", "ARTIST")
                putExtra("ARTIST_ID", artist.id)
                putExtra("ARTIST_NAME", artist.name)
            }
            startActivity(intent)
        }
        recyclerView.adapter = artistAdapter

        artistsViewModel.artists.observe(viewLifecycleOwner) { artists ->
            Log.d("ArtistsFragment", "Artists: ${artists.size}")
            artistsList.clear()
            artistsList.addAll(artists)
            originalArtistList = artists
            artistAdapter.notifyDataSetChanged()
        }

        return view
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
            if (artistsList.isEmpty()) {
                artistsViewModel.loadArtists()
            }
        }
    }

    override fun onSearchQuery(query: String?) {
        query?.let {
            val filteredList = if (it.isNotBlank()) {
                originalArtistList.filter { artist ->
                    artist.name.contains(it, ignoreCase = true)
                }
            } else {
                originalArtistList

            }
            updateArtistListAdapter(filteredList)

        }
    }

    fun updateArtistListAdapter(filteredList: List<Artist>) {
        artistsList.clear()
        artistsList.addAll(filteredList)
        artistAdapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(0)
    }

    override fun onSortBy(sortBy: String) {
        when (sortBy) {
            "Name" -> {
                val sortedList = originalArtistList.sortedBy { it.name }
                updateArtistListAdapter(sortedList)
            }

            "Album count" -> {
                val sortedList = originalArtistList.sortedBy { it.numberOfAlbums }
                updateArtistListAdapter(sortedList)
            }

            "Song count" -> {
                val sortedList = originalArtistList.sortedBy { it.numberOfTracks }
                updateArtistListAdapter(sortedList)
            }
        }
    }

    override fun getSortOptions(): List<String> {
        return listOf("Name", "Album count", "Song count") // Example options
    }
}