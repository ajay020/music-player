package com.example.musicplayer.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Helper

class SongAdapter(
    private val songs: List<Song>,
    private val onSongClick: (Song, Int) -> Unit,
    private val onMoreOptionsClick: (Song) -> Unit = {},
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    var currentPlayingIndex: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        SongViewHolder(view)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        val isPlaying = currentPlayingIndex == position

        holder.bind(song, position, onSongClick, onMoreOptionsClick, isPlaying)
    }

    // ViewHolder for Song Item
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.song_title)
        val artistTextView: TextView = itemView.findViewById(R.id.song_artist)
        val durationTextView: TextView = itemView.findViewById(R.id.song_duration)
        val albumArtImageView: ImageView = itemView.findViewById(R.id.song_art)
        val moreOptionsImageView: ImageView = itemView.findViewById(R.id.more_options)

        fun bind(
            song: Song,
            index: Int,
            onClick: (Song, Int) -> Unit,
            onMoreOptionsClick: (Song) -> Unit,
            isPlaying: Boolean
        ) {
            titleTextView.text = song.title
            itemView.setOnClickListener { onClick(song, index) }
            artistTextView.text = song.artist
            durationTextView.text = Helper.formatTime(song.duration)
            setImageView(albumArtImageView, song.uri.toString())

            // Highlight logic here
            if (isPlaying) {
                titleTextView.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.highlight
                    )
                )
                artistTextView.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.highlight
                    )
                )

                durationTextView.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.highlight
                    )
                )
            } else {
                titleTextView.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.md_theme_onBackground
                    )
                )
                artistTextView.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.md_theme_onBackground
                    )
                )
                durationTextView.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.md_theme_onBackground
                    )
                )
            }

            moreOptionsImageView.setOnClickListener {
                onMoreOptionsClick(song) // Call the new callback
            }
        }
    }

    override fun getItemCount() = songs.size

    private fun setImageView(imageView: ImageView, uri: String) {
        val bitmap = Helper.getEmbeddedAlbumArt(context = imageView.context, songUri = uri.toUri())
        Glide.with(imageView.context)
            .load(bitmap)
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
