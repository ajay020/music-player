package com.example.musicplayer.ui.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.R
import dagger.hilt.android.AndroidEntryPoint
import androidx.media3.common.Player

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var btnPlayPause: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var txtCurrentTime: TextView
    private lateinit var txtTotalTime: TextView
    private lateinit var txtSongTitle: TextView
    private lateinit var txtArtistName: TextView

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        initializeUI()
        initializePlayer()
        setupControls()
        updateSeekBar()
    }

    private fun initializeUI() {
        btnPlayPause = findViewById(R.id.btnPlayPause)
        seekBar = findViewById(R.id.seekBar)
        txtCurrentTime = findViewById(R.id.txtCurrentTime)
        txtTotalTime = findViewById(R.id.txtTotalTime)
        txtSongTitle = findViewById(R.id.txtSongTitle)
        txtArtistName = findViewById(R.id.txtArtistName)

        // Get song details from Intent
        val songTitle = intent.getStringExtra("SONG_TITLE") ?: "Unknown Title"
        val songArtist = intent.getStringExtra("SONG_ARTIST") ?: "Unknown Artist"
        txtSongTitle.text = songTitle
        txtArtistName.text = songArtist
    }

    private fun initializePlayer() {
        val songPath = intent.getStringExtra("SONG_PATH") ?: return

        player = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(songPath)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    seekBar.max = player.duration.toInt()
                    txtTotalTime.text = formatTime(player.duration)
                }
            }
        })
    }

    private fun setupControls() {
        btnPlayPause.setOnClickListener {
            if (player.isPlaying) pausePlayer() else playPlayer()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) player.seekTo(progress.toLong())
                txtCurrentTime.text = formatTime(player.currentPosition)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playPlayer() {
        player.play()
        btnPlayPause.setImageResource(R.drawable.ic_pause)
    }

    private fun pausePlayer() {
        player.pause()
        btnPlayPause.setImageResource(R.drawable.ic_play)
    }

    private fun updateSeekBar() {
        handler.post(object : Runnable {
            override fun run() {
                if (player.isPlaying) {
                    txtCurrentTime.text = formatTime(player.currentPosition)
                    seekBar.progress = player.currentPosition.toInt()
                }
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
        player.release()
        handler.removeCallbacksAndMessages(null) // Stop updates when activity is destroyed
    }
}

