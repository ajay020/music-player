package com.example.musicplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Artist

class ArtistAdapter(private val artists: List<Artist>, private val onItemClick: (Artist) -> Unit) :
    RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.artist_name)
        val infoTextView: TextView = itemView.findViewById(R.id.artist_info)

        fun bind(artist: Artist) {
            nameTextView.text = artist.name
            infoTextView.text = "${artist.numberOfAlbums} Albums • ${artist.numberOfTracks} Tracks"
            itemView.setOnClickListener { onItemClick(artist) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(artists[position])
    }

    override fun getItemCount() = artists.size
}