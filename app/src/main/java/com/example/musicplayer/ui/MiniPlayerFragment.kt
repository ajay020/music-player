package com.example.musicplayer.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Helper
import com.example.musicplayer.viewmodel.MusicViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MiniPlayerFragment : Fragment(R.layout.fragment_mini_player) {
    private lateinit var miniSongTitle: TextView
    private lateinit var miniArtistName: TextView
    private lateinit var miniAlbumArt: ImageView
    private lateinit var miniButtonPlayPause: ImageButton
    private lateinit var miniButtonPrev: ImageButton
    private lateinit var miniButtonNext: ImageButton

    private val musicViewModel: MusicViewModel by activityViewModels()

    private val songReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val song = intent?.getParcelableExtra<Song>("SONG")
            val isPlaying = intent?.getBooleanExtra("IS_PLAYING", false) ?: false

            song?.let {
                updateMiniPlayer(it)
                updatePlayPauseButton(isPlaying)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        miniSongTitle = view.findViewById(R.id.mini_song_title)
        miniArtistName = view.findViewById(R.id.mini_artist_name)
        miniAlbumArt = view.findViewById(R.id.mini_album_art)
        miniButtonPlayPause = view.findViewById(R.id.mini_btn_play_pause)
        miniButtonPrev = view.findViewById(R.id.mini_btn_prev)
        miniButtonNext = view.findViewById(R.id.mini_btn_next)

        miniButtonPlayPause.setOnClickListener { musicViewModel.togglePlayPause() }
        miniButtonPrev.setOnClickListener { musicViewModel.previous() }
        miniButtonNext.setOnClickListener { musicViewModel.next() }

        ContextCompat.registerReceiver(
            requireContext(),
            songReceiver,
            IntentFilter("ACTION_SONG_CHANGED"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(songReceiver)
    }

    private fun updateMiniPlayer(song: Song) {
        miniSongTitle.text = song.title
        miniArtistName.text = song.artist

        val albumArt = Helper.getAlbumArt(song.uri, requireContext())
        if (albumArt != null) {
            miniAlbumArt.setImageBitmap(albumArt)
        } else {
            miniAlbumArt.setImageResource(R.drawable.ic_music_placeholder)
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        val resId = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        miniButtonPlayPause.setImageResource(resId)
    }
}
