package com.example.musicplayer.ui.player

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import dagger.hilt.android.AndroidEntryPoint
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Helper
import com.example.musicplayer.viewmodel.MusicViewModel
import com.example.musicplayer.viewmodel.PlayerViewModel
import com.google.android.material.button.MaterialButton
import java.util.Locale

private const val TAG = "PlayerActivity"

@AndroidEntryPoint
class FullPlayerActivity : AppCompatActivity() {
    private lateinit var albumArt: ImageView
    private lateinit var songTitle: TextView
    private lateinit var artistName: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var btnPrev: ImageButton
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var currentTimeTextView: TextView
    private lateinit var totalDurationTextView: TextView
    private lateinit var btnClose: MaterialButton

    private val musicViewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_player)

        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun initViews() {
        albumArt = findViewById(R.id.full_album_art)
        songTitle = findViewById(R.id.full_song_title)
        artistName = findViewById(R.id.full_artist_name)
        seekBar = findViewById(R.id.seek_bar)
        btnPrev = findViewById(R.id.btn_prev)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnNext = findViewById(R.id.btn_next)
        currentTimeTextView = findViewById(R.id.current_time)
        totalDurationTextView = findViewById(R.id.total_duration)
        btnClose = findViewById(R.id.btnClose)
    }

    private fun setupListeners() {
        btnClose.setOnClickListener {
            finish() // This will close the current activity
        }

        btnPlayPause.setOnClickListener {
            musicViewModel.togglePlayPause()
        }

        btnPrev.setOnClickListener {
            musicViewModel.previous()
        }

        btnNext.setOnClickListener {
            musicViewModel.next()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    Log.d("SeekBar", "Progress changed to $progress")
                    musicViewModel.seekTo(progress)
                    updateTimeDisplay(
                        progress.toLong(),
                        seekBar.max.toLong()
                    ) // Update time immediately on seek
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun observeViewModel() {
        musicViewModel.currentSong.observe(this) { song ->
            song?.let {
                songTitle.text = song.title
                artistName.text = song.artist
                Glide.with(this)
                    .load(song.uri)
                    .placeholder(R.drawable.ic_music_placeholder)
                    .error(R.drawable.ic_music_placeholder)
                    .into(albumArt)
            }
        }

        musicViewModel.isPlaying.observe(this) { playing ->
            btnPlayPause.setImageResource(
                if (playing) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        musicViewModel.currentPosition.observe(this) { position ->
            Log.d("SeekBar", "Progress position $position")
            seekBar.progress = position
            updateTimeDisplay(position.toLong(), seekBar.max.toLong()) // Update current time
        }

        musicViewModel.duration.observe(this) { duration ->
            Log.d("SeekBar", "Progress duration $duration")
            seekBar.max = duration
            updateTimeDisplay(
                seekBar.progress.toLong(),
                duration.toLong()
            ) // Update total duration initially
        }
    }

    private fun updateTimeDisplay(currentPosition: Long, totalDuration: Long) {
        currentTimeTextView.text = Helper.formatTime(currentPosition)
        totalDurationTextView.text = Helper.formatTime(totalDuration)
    }
}
