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
import com.example.musicplayer.data.model.Album
import com.example.musicplayer.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumsFragment : Fragment() {
    private val viewModel: AlbumViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var albumAdapter: AlbumAdapter
    private var albumList = mutableListOf<Album>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view  =  inflater.inflate(R.layout.fragment_albums, container, false)

        recyclerView = view.findViewById(R.id.album_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        albumAdapter = AlbumAdapter(albumList ) { album ->
            val intent = Intent(requireContext(), AlbumSongsActivity::class.java)
            intent.putExtra("albumId", album.id)
            intent.putExtra("albumName", album.name)
            intent.putExtra("albumCoverUri", album.coverUri.toString())
            startActivity(intent)
        }
        recyclerView.adapter = albumAdapter

        viewModel.albums.observe(viewLifecycleOwner) { albums ->
            albumList.clear()  // Clear the old list before adding new albums
            albumList.addAll(albums)
            albumAdapter.notifyDataSetChanged() // Notify adapter about the new data
        }

        // Load albums from the ViewModel
        viewModel.loadAlbums()

        return view
    }
}