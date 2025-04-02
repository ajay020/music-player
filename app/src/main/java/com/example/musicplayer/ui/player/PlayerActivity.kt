package com.example.musicplayer.ui.player

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
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.R
import dagger.hilt.android.AndroidEntryPoint
import androidx.media3.common.Player
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Helper
import com.example.musicplayer.viewmodel.PlayerViewModel

private const val TAG = "PlayerActivity"

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var txtCurrentTime: TextView
    private lateinit var txtTotalTime: TextView
    private lateinit var txtSongTitle: TextView
    private lateinit var txtArtistName: TextView

    private val handler = Handler(Looper.getMainLooper())

    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        getSongFromIntent()
        initializeUI()
        setupObservers()
        setupControls()
    }

    private fun getSongFromIntent() {
        val songId = intent.getLongExtra("currentSongId", -1)
        val playlistType = intent.getStringExtra("playlistType")
        val playlistId = intent.getStringExtra("playlistId")

        if (songId != -1L && playlistType != null) {
            viewModel.setCurrentPlaylistInfo( playlistType, playlistId)
            viewModel.loadSongs(playlistType, playlistId)
            val song = viewModel.songs.value?.find { it.id == songId }
            if (song != null) {
                viewModel.setCurrentSong(song)
                setAlbumCover(song)

            } else {
                Log.e(TAG, "Song not found with ID: $songId")
            }
        }
    }

    private fun setAlbumCover(song: Song) {
        val albumArt = Helper.getAlbumArt(song.uri, this)
        val imageView: ImageView = findViewById(R.id.imgAlbumCover)

        if (albumArt != null) {
            imageView.setImageBitmap(albumArt)
        } else {
            imageView.setImageResource(R.drawable.ic_music_placeholder)
        }
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
    }

    private fun setupObservers() {
        // Observe the current song in the ViewModel
        viewModel.currentSong.observe(this) { song ->
            // Update UI with song information
            txtSongTitle.text = song.title
            txtArtistName.text = song.artist
            setAlbumCover(song)

            // Initialize or update player with the new song
            if (!::player.isInitialized) {
                initializePlayer(song)
            } else {
                updatePlayer(song)
            }
        }
    }

    private fun initializePlayer(song: Song) {
        player = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(song.path)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    seekBar.max = player.duration.toInt()
                    txtTotalTime.text = formatTime(player.duration)
                    updateSeekBar()
                } else if (state == Player.STATE_ENDED) {
                    // Auto play next song when current song ends
                    playNextSong()
                }
            }
        })
    }

    private fun updatePlayer(song: Song) {
        val wasPlaying = player.isPlaying
        // Stop current playback
        player.stop()

        // Set new media item
        val mediaItem = MediaItem.fromUri(song.path)
        player.setMediaItem(mediaItem)
        player.prepare()

        if (wasPlaying) {
            // Resume playback if it was playing before
            player.play()
        } else {
            // Pause playback if it was not playing before
            player.pause()
        }

        // Reset UI controls
        seekBar.progress = 0
        txtCurrentTime.text = "00:00"
    }

    private fun setupControls() {
        btnPlayPause.setOnClickListener {
            if (player.isPlaying) pausePlayer() else playPlayer()
        }

        btnPrevious.setOnClickListener {
            playPreviousSong()
        }

        btnNext.setOnClickListener {
            playNextSong()
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

    private fun playPreviousSong() {
        val previousSong = viewModel.getPreviousSong() ?: return
        viewModel.setCurrentSong(previousSong)
    }

    private fun playNextSong() {
        val nextSong = viewModel.getNextSong() ?: return
        viewModel.setCurrentSong(nextSong)
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
                if (::player.isInitialized && player.isPlaying) {
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
        return String.format( "%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.release()
        }
        // Stop updates when activity is destroyed
        handler.removeCallbacksAndMessages(null)
    }
}
