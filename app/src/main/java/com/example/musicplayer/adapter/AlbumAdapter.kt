package com.example.musicplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Album

class AlbumAdapter(
    private val albums: List<Album>,
    private val onAlbumClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    inner class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val albumName: TextView = view.findViewById(R.id.album_name)
        val songCount: TextView = view.findViewById(R.id.song_count)
        val albumCover: ImageView = view.findViewById(R.id.album_cover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]
        holder.albumName.text = album.name
        holder.songCount.text = "${album.songCount} songs"

        // Load album cover using Glide
        Glide.with(holder.itemView.context)
            .load(album.coverUri)
            .placeholder(R.drawable.ic_music_placeholder)  // A default image in case of no album art
            .error(R.drawable.ic_music_placeholder)  // If loading fails
            .into(holder.albumCover)

        holder.itemView.setOnClickListener { onAlbumClick(album) }
    }

    override fun getItemCount() = albums.size
}
