package com.example.musicplayer.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Playlist

class PlaylistAdapter(
    private val playlists: List<Playlist>,
    private val onItemClick: (Playlist) -> Unit,
    private val onMoreOptionsClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistImageView: ImageView = itemView.findViewById(R.id.playlist_image)
        val nameTextView: TextView = itemView.findViewById(R.id.playlist_name)
        val songsCountTextView: TextView = itemView.findViewById(R.id.songs_count)
        val moreOptionsImageView: ImageView = itemView.findViewById(R.id.more_options)

        fun bind(playlist: Playlist) {
            nameTextView.text = playlist.name
            songsCountTextView.text = "${playlist.songCount} Songs"
            itemView.setOnClickListener { onItemClick(playlist) }

            when (playlist.playlistType) {
                Playlist.PlaylistType.FAVORITES -> {
                    playlistImageView.setImageResource(R.drawable.ic_favorite_playlist) // Replace with your icon
                }

                Playlist.PlaylistType.RECENTLY_ADDED -> {
                    playlistImageView.setImageResource(R.drawable.ic_recently_added_playlist) // Replace with your icon
                }

                Playlist.PlaylistType.MOST_PLAYED -> {
                    playlistImageView.setImageResource(R.drawable.ic_most_played_playlist) // Replace with your icon
                }

                else -> {
                    // Load custom image if available for user-created playlists
                    playlist.imageUri?.let { uri ->
                        Glide.with(itemView.context)
                            .load(uri)
                            .placeholder(R.drawable.ic_music_placeholder)
                            .error(R.drawable.ic_music_placeholder)
                            .into(playlistImageView)
                    } ?: run {
                        playlistImageView.setImageResource(R.drawable.ic_music_placeholder)
                    }
                }
            }

            // Style for default playlists (optional)
            if (playlist.isDefault) {
                nameTextView.setTypeface(null, Typeface.BOLD)
            } else {
                nameTextView.setTypeface(null, Typeface.NORMAL)
                // Set OnClickListener for the more options icon
                moreOptionsImageView.setOnClickListener {
                    onMoreOptionsClick(playlist)
                }
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount() = playlists.size
}