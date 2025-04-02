package com.example.musicplayer.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.databinding.ItemSongBinding
import com.example.musicplayer.utils.Helper
import java.util.concurrent.TimeUnit

class SongAdapter(
    private val songs: List<Song>,
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.song_title)
        val artistTextView: TextView = itemView.findViewById(R.id.song_artist)
        val durationTextView: TextView = itemView.findViewById(R.id.song_duration)
        val albumArtImageView: ImageView = itemView.findViewById(R.id.song_art)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        holder.titleTextView.text = song.title
        holder.artistTextView.text = song.artist
        holder.durationTextView.text = formatDuration(song.duration)

        val bitmap = Helper.getAlbumArt( song.uri, holder.itemView.context,)
        if (bitmap != null) {
            holder.albumArtImageView.setImageBitmap(bitmap)
        }else {
            holder.albumArtImageView.setImageResource(R.drawable.ic_music_placeholder)
        }

        holder.itemView.setOnClickListener {
            onSongClick(song)
        }
    }

    override fun getItemCount() = songs.size

    private fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
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
