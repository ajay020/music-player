package com.example.musicplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.data.model.FolderWithSongCount

class FolderAdapter(private val onItemClick: (String) -> Unit = {}) :
    ListAdapter<FolderWithSongCount, FolderAdapter.FolderViewHolder>(FolderDiffCallback()) {

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderNameTextView: TextView = itemView.findViewById(R.id.folder_name)
        val songCountTextView: TextView = itemView.findViewById(R.id.song_count)

        init {
            itemView.setOnClickListener {
                getItem(adapterPosition)?.let { folder ->
                    onItemClick(folder.folderName)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.folderNameTextView.text = currentItem.folderName
        holder.songCountTextView.text = "${currentItem.songCount} songs"
    }
}

class FolderDiffCallback : DiffUtil.ItemCallback<FolderWithSongCount>() {
    override fun areItemsTheSame(
        oldItem: FolderWithSongCount,
        newItem: FolderWithSongCount
    ): Boolean {
        return oldItem.folderName == newItem.folderName
    }

    override fun areContentsTheSame(
        oldItem: FolderWithSongCount,
        newItem: FolderWithSongCount
    ): Boolean {
        return oldItem == newItem
    }
}