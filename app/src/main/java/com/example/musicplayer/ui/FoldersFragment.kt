package com.example.musicplayer.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musicplayer.R
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.adapter.FolderAdapter
import com.example.musicplayer.data.model.FolderWithSongCount
import com.example.musicplayer.data.model.Searchable
import com.example.musicplayer.viewmodel.FolderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FoldersFragment : Fragment(), Searchable {

    private val folderViewModel: FolderViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter
    private val folderList = mutableListOf<FolderWithSongCount>()
    private var originalFolderList = emptyList<FolderWithSongCount>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_folders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.folder_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        folderAdapter = FolderAdapter({ folderName ->

            val intent = Intent(requireContext(), SongsDisplayActivity::class.java).apply {
                putExtra("TYPE", "FOLDER")
                putExtra("FOLDER_NAME", folderName)
            }
            startActivity(intent)

        }) // Initialize with an empty list
        recyclerView.adapter = folderAdapter

        observeFolderData()
    }

    private fun observeFolderData() {
        folderViewModel.folderWithSongCount.observe(viewLifecycleOwner, { folders ->
            folderAdapter.submitList(folders)
            folderList.clear()
            folderList.addAll(folders)
            originalFolderList = folders
        })
    }

    override fun onSearchQuery(query: String?) {

        query?.let {
            val filteredList = if (it.isNotBlank()) {
                originalFolderList.filter { folder ->
                    folder.folderName.contains(it, ignoreCase = true)
                }
            } else {
                originalFolderList

            }

            updateFolderListAdapter(filteredList)
        }
    }

    fun updateFolderListAdapter(filteredList: List<FolderWithSongCount>) {
        folderList.clear()
        folderList.addAll(filteredList)
        folderAdapter.submitList(filteredList)
        folderAdapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(0)
    }
}