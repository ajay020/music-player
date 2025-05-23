package com.example.musicplayer.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.adapter.AlbumAdapter
import com.example.musicplayer.data.model.Album
import com.example.musicplayer.data.model.Searchable
import com.example.musicplayer.data.model.Sortable
import com.example.musicplayer.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumsFragment : Fragment(), Searchable, Sortable {
    private val viewModel: AlbumViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var albumAdapter: AlbumAdapter
    private var albumList = mutableListOf<Album>()
    private var originalAlbumList = emptyList<Album>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_albums, container, false)

        recyclerView = view.findViewById(R.id.album_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        albumAdapter = AlbumAdapter(albumList) { album ->
            val intent = Intent(requireContext(), SongsDisplayActivity::class.java).apply {
                putExtra("TYPE", "ALBUM")
                putExtra("ALBUM_ID", album.id)
                putExtra("ALBUM_NAME", album.name)
            }

            startActivity(intent)
        }
        recyclerView.adapter = albumAdapter

        viewModel.albums.observe(viewLifecycleOwner) { albums ->
            albumList.clear()  // Clear the old list before adding new albums
            albumList.addAll(albums)
            originalAlbumList = albums
            albumAdapter.notifyDataSetChanged() // Notify adapter about the new data
        }

        // Load albums from the ViewModel
        viewModel.loadAlbums()

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
            if (albumList.isEmpty()) {
                viewModel.loadAlbums()
            }
        }
    }

    override fun onSearchQuery(query: String?) {
        query?.let {
            val filteredList = if (it.isNotBlank()) {
                originalAlbumList.filter { album ->
                    album.name.contains(it, ignoreCase = true)
                }
            } else {
                originalAlbumList
            }
            updateAlbumListAdapter(filteredList)
        }
    }

    private fun updateAlbumListAdapter(filteredList: List<Album>) {
        albumList.clear()
        albumList.addAll(filteredList)
        albumAdapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(0)
    }

    override fun onSortBy(sortBy: String) {
        when (sortBy) {
            "Name" -> {
                val sortedList = originalAlbumList.sortedBy { it.name }
                updateAlbumListAdapter(sortedList)
            }

            "Song count" -> {
                val sortedList = originalAlbumList.sortedBy { it.songCount }
                updateAlbumListAdapter(sortedList)
            }
        }
    }

    override fun getSortOptions(): List<String> {
        return listOf("Name", "Song count")
    }
}