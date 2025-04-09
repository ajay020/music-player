package com.example.musicplayer.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Helper

class SongAdapter(
    private val songs: List<Song>,
    private val albumCoverUri: String = "",
    private val albumName: String = "",
    private val onSongClick: (Song, Int) -> Unit,
    private val onBackButtonClick: () -> Unit = {},
    private val onSearchButtonClick: () -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_SONG = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && albumCoverUri != "") VIEW_TYPE_HEADER else VIEW_TYPE_SONG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_album_header, parent, false)
            AlbumHeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_song, parent, false)
            SongViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val song = songs[position]

        if (holder is AlbumHeaderViewHolder) {
            holder.bind(albumCoverUri)

        } else if (holder is SongViewHolder) {
            holder.bind(song, position, onSongClick)
        }
    }

    // ViewHolder for Album Cover & Title
    inner class AlbumHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
        private val albumTitle: TextView = itemView.findViewById(R.id.tv_album_title)
        private val backButton: ImageButton = itemView.findViewById(R.id.back_button)
        private val searchButton: ImageButton = itemView.findViewById(R.id.btn_search)

        fun bind(albumCoverUri: String) {
            setImageView(albumCover, albumCoverUri)
            albumTitle.text = albumName

            backButton.setOnClickListener {
                onBackButtonClick() // Call the callback
            }

            searchButton.setOnClickListener {
                onSearchButtonClick() // Call the search callback
            }
        }
    }

    // ViewHolder for Song Item
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.song_title)
        val artistTextView: TextView = itemView.findViewById(R.id.song_artist)
        val durationTextView: TextView = itemView.findViewById(R.id.song_duration)
        val albumArtImageView: ImageView = itemView.findViewById(R.id.song_art)

        fun bind(song: Song, index: Int, onClick: (Song, Int) -> Unit) {
            titleTextView.text = song.title
            itemView.setOnClickListener { onClick(song, index) }
            artistTextView.text = song.artist
            durationTextView.text = Helper.formatTime(song.duration)
            setImageView(albumArtImageView, song.uri.toString())
        }
    }

    override fun getItemCount() = songs.size

    private fun setImageView(imageView: ImageView, uri: String) {
        Glide.with(imageView.context)
            .load(uri)
            .placeholder(R.drawable.ic_music_placeholder)
            .error(R.drawable.ic_music_placeholder)
            .into(imageView)
    }
}

class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem.path == newItem.path  // Assuming path is unique for each song
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }
}
