package com.example.musicplayer.ui.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.R
import dagger.hilt.android.AndroidEntryPoint
import androidx.media3.common.Player
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.service.MusicService
import com.example.musicplayer.utils.Helper
import com.example.musicplayer.viewmodel.PlayerViewModel

private const val TAG = "PlayerActivity"

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var txtCurrentTime: TextView
    private lateinit var txtTotalTime: TextView
    private lateinit var txtSongTitle: TextView
    private lateinit var txtArtistName: TextView
    private lateinit var imgAlbumCover: ImageView

    private val handler = Handler(Looper.getMainLooper())

    private val viewModel: PlayerViewModel by viewModels()

    // Music service
    private var musicService: MusicService? = null
    private var bound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        initializeUI()
        setupObservers()
        setupControls()
    }

    private fun initializeUI() {
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnPrevious = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        seekBar = findViewById(R.id.seekBar)
        txtCurrentTime = findViewById(R.id.txtCurrentTime)
        txtTotalTime = findViewById(R.id.txtTotalTime)
        txtSongTitle = findViewById(R.id.txtSongTitle)
        txtArtistName = findViewById(R.id.txtArtistName)
        imgAlbumCover = findViewById(R.id.imgAlbumCover)
    }

    private fun setupObservers() {
        viewModel.currentSong.observe(this) { song ->
            // Only update UI when we're not requesting to play the song
            // (to avoid overriding the UI update from the service callback)
            if (!viewModel.isPlayRequested) {
                updateUIWithSong(song)
            }
        }
    }

    private fun updateUIWithSong(song: Song) {
        txtSongTitle.text = song.title
        txtArtistName.text = song.artist
        setAlbumCover(song)

//        musicService?.let {
//            seekBar.max = it.getDuration().toInt()
//            txtTotalTime.text = formatTime(it.getDuration())
//        }
    }

    private fun setAlbumCover(song: Song) {
        val albumArt = Helper.getAlbumArt(song.uri, this)

        if (albumArt != null) {
            imgAlbumCover.setImageBitmap(albumArt)
        } else {
            imgAlbumCover.setImageResource(R.drawable.ic_music_placeholder)
        }
    }

    private fun setupControls() {
        btnPlayPause.setOnClickListener {
//            musicService?.resumeOrPause()
        }

        btnPrevious.setOnClickListener {
            viewModel.playPreviousSong()
        }

        btnNext.setOnClickListener {
            viewModel.playNextSong()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                if (fromUser) musicService?.seekTo(progress.toLong())
                txtCurrentTime.text = formatTime(progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        if (isPlaying) {
            btnPlayPause.setImageResource(R.drawable.ic_pause)
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_play)
        }
    }

    private fun updateSeekBar() {
        handler.post(object : Runnable {
            override fun run() {
//                musicService?.let {
//                    txtCurrentTime.text = formatTime(it.getCurrentPosition())
//                    seekBar.progress = it.getCurrentPosition().toInt()
//                }
                handler.postDelayed(this, 1000) // Update every second
            }
        })
    }

    private fun formatTime(milliseconds: Long): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
//        if (bound) {
//            unbindService(serviceConnection)
//            bound = false
//        }
        handler.removeCallbacksAndMessages(null)
    }
}