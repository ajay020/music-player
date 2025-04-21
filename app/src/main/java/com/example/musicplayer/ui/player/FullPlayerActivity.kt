package com.example.musicplayer.ui.player

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.musicplayer.R
import dagger.hilt.android.AndroidEntryPoint
import com.example.musicplayer.utils.Helper
import com.example.musicplayer.viewmodel.MusicViewModel
import com.google.android.material.button.MaterialButton
import javax.sql.DataSource

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

                Log.d("URI", "Uri: ${song.uri}")
                val bitmap = Helper.getEmbeddedAlbumArt(this, song.uri)

                Glide.with(this)
                    .load(bitmap)
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
            seekBar.progress = position
            updateTimeDisplay(position.toLong(), seekBar.max.toLong()) // Update current time
        }

        musicViewModel.duration.observe(this) { duration ->
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
