package com.example.musicplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.session.MediaSession
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.MainActivity
import com.example.musicplayer.R
import com.example.musicplayer.data.SongManager
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.utils.Helper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicService : Service() {
    private lateinit var player: ExoPlayer
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "MusicPlayerChannel"

    private val handler = Handler(Looper.getMainLooper())
    private val updateIntervalMillis: Long = 500 // Update every 200 milliseconds
    private val updateRunnable = object : Runnable {
        override fun run() {
            SongManager.getCurrentSong()?.let {
                broadcastCurrentState(it)
            }
            handler.postDelayed(this, updateIntervalMillis) // every second
        }
    }

    private fun broadcastCurrentState(song: Song) {
        val intent = Intent("PLAYER_STATE")
        intent.putExtra("SONG", song)
        intent.putExtra("IS_PLAYING", player.isPlaying)
        intent.putExtra("CURRENT_POSITION", player.currentPosition)
        intent.putExtra("DURATION", player.duration)

        Log.d("MusicService", "Broadcasting: isPlaying=${player.isPlaying}, position=${player.currentPosition}, duration=${player.duration}")

        sendBroadcast(intent)
    }


    override fun onCreate() {
        super.onCreate()
        handler.post(updateRunnable)
        createNotificationChannel()
        player = ExoPlayer.Builder(this).build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                SongManager.getCurrentSong()?.let { song ->
                    updateNotification(song)
                    broadcastCurrentState(song)
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MusicService", "Service started with ${intent?.action}}")

        when (intent?.action) {
            "ACTION_PLAY" -> {
                SongManager.getCurrentSong()?.let { song ->
                    playSong(song)
                    broadcastCurrentState(song)
                }
            }

            "ACTION_NEXT" -> {
                SongManager.getNextSong()?.let { song ->
                    playSong(song)
                    updateNotification(song)
                    broadcastCurrentState(song)
                }
            }

            "ACTION_PREVIOUS" -> {
                SongManager.getPreviousSong()?.let { song ->
                    playSong(song)
                    updateNotification(song)
                    broadcastCurrentState(song)
                }
            }

            "TOGGLE_PLAY_PAUSE" -> {
                resumeOrPause()
            }

            "ACTION_SEEK_TO" -> {
                val position = intent.getIntExtra("seekTo", 0)
                Log.d( "MusicService", "Seek to position: $position")
                player.seekTo(position.toLong())
            }
        }
        return START_STICKY
    }

    fun playSong(song: Song) {
        val mediaItem = MediaItem.fromUri(song.path)
        player.setMediaItem(mediaItem)
        player.prepare()

        player.play()

        // Start foreground service with notification
        startForegroundService(song)
        updateNotification(song)
    }

    fun resumeOrPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }

        SongManager.getCurrentSong()?.let { song ->
            updateNotification(song)
            broadcastCurrentState(song)
        }
    }

    private fun startForegroundService(song: Song) {
        val notification = createNotification(song)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Music Player"
            val descriptionText = "Music player controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(song: Song): Notification {
        // Create pending intents for notification actions
        val playIntent = PendingIntent.getService(
            this, 0,
            Intent(this, MusicService::class.java).setAction("TOGGLE_PLAY_PAUSE"),
            PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getService(
            this, 1,
            Intent(this, MusicService::class.java).setAction("ACTION_PREVIOUS"),
            PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this, 2,
            Intent(this, MusicService::class.java).setAction("ACTION_NEXT"),
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 3,
            Intent(this, MusicService::class.java).setAction("ACTION_STOP"),
            PendingIntent.FLAG_IMMUTABLE
        )

        // Create intent to open the app when notification is clicked
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        // Get album art for notification
        val albumArt = Helper.getAlbumArt(song.uri, this)
        val bitmap =
            albumArt ?: BitmapFactory.decodeResource(resources, R.drawable.ic_music_placeholder)

        // Build notification
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, CHANNEL_ID)
        } else {
            NotificationCompat.Builder(this)
        }

        return builder
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(R.drawable.ic_music_placeholder)
            .setLargeIcon(bitmap)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_prev, "Previous", prevIntent)
            .addAction(
                if (player.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (player.isPlaying) "Pause" else "Play",
                playIntent
            )
            .addAction(R.drawable.ic_next, "Next", nextIntent)
            .addAction(R.drawable.ic_close, "Close", stopIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(song: Song) {
        val notification = createNotification(song)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}