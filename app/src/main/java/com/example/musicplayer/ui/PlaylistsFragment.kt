package com.example.musicplayer.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.adapter.PlaylistAdapter
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.model.Searchable
import com.example.musicplayer.viewmodel.PlaylistViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistsFragment : Fragment(), Searchable {
    private val playlistViewModel: PlaylistViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var fabAddPlaylist: FloatingActionButton
    private val originalPlaylist = mutableListOf<Playlist>()
    private val playlistList = mutableListOf<Playlist>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlists, container, false)
        recyclerView = view.findViewById(R.id.playlists_recycler_view)
        fabAddPlaylist = view.findViewById(R.id.fab_add_playlist)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        playlistAdapter = PlaylistAdapter(
            playlistList, { playlist ->
                val intent = Intent(requireContext(), SongsDisplayActivity::class.java).apply {
                    putExtra("TYPE", "PLAYLIST")
                    putExtra("PLAYLIST_ID", playlist.id)
                    putExtra("PLAYLIST_NAME", playlist.name)
                }
                startActivity(intent)
            },
            { playlist -> showPlaylistOptionsDialog(playlist) }
        )
        recyclerView.adapter = playlistAdapter

        // Observe all playlists
        playlistViewModel.allPlaylists.observe(viewLifecycleOwner) { playlists ->
            playlistList.clear()
            originalPlaylist.clear()

            originalPlaylist.addAll(playlists)
            playlistList.addAll(playlists)

            playlistAdapter.notifyDataSetChanged()
        }

        fabAddPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }

        return view
    }

    private fun showCreatePlaylistDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Create New Playlist")

        val input = EditText(requireContext())
        builder.setView(input)

        builder.setPositiveButton("Create") { dialog, _ ->
            val playlistName = input.text.toString().trim()
            if (playlistName.isNotEmpty()) {
                val newPlaylist = Playlist(
                    name = playlistName,
                    isDefault = false,
                    playlistType = null,
                    songCount = 0,
                    imageUri = null,
                )
                playlistViewModel.insertPlaylist(newPlaylist)
                dialog.dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Playlist name cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showPlaylistOptionsDialog(playlist: Playlist) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(playlist.name)
        val options = arrayOf("Edit Name", "Delete") // Add more options as needed
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> showEditPlaylistNameDialog(playlist)
                1 -> showDeletePlaylistConfirmationDialog(playlist)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showEditPlaylistNameDialog(playlist: Playlist) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Playlist Name")
        val input = EditText(requireContext())
        input.setText(playlist.name)
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                playlistViewModel.updatePlaylist(playlist.copy(name = newName))
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun showDeletePlaylistConfirmationDialog(playlist: Playlist) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Playlist")
        builder.setMessage("Are you sure you want to delete '${playlist.name}'?")
        builder.setPositiveButton("Delete") { _, _ ->
            playlistViewModel.deletePlaylist(playlist)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    override fun onSearchQuery(query: String?) {
        query?.let {
            val filteredList = if (it.isNotBlank()) {
                originalPlaylist.filter { playlist ->
                    playlist.name.contains(it, ignoreCase = true)
                }
            } else {
                originalPlaylist
            }

            updatePlaylistAdapter(filteredList)
        }
    }

    private fun updatePlaylistAdapter(filteredList: List<Playlist>) {
        playlistList.clear()
        playlistList.addAll(filteredList)
        playlistAdapter.notifyDataSetChanged()
    }
}
