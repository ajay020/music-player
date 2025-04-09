package com.example.musicplayer.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.SongManager
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repository.SongRepository
import com.example.musicplayer.service.MusicService
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songRepository: SongRepository
) : ViewModel() {
    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> = _currentSong

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> get() = _currentPosition

    private val _duration = MutableLiveData<Int>()
    val duration: LiveData<Int> get() = _duration

    private val serviceIntent = Intent(context, MusicService::class.java)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val isPlaying = it.getBooleanExtra("IS_PLAYING", false)
                val currentPosition = it.getLongExtra("CURRENT_POSITION", 0)
                val duration = it.getLongExtra("DURATION", 0)
                val song = it.getParcelableExtra<Song>("SONG")
                Log.d(
                    "MusicViewModel",
                    "Received: isPlaying=$isPlaying, position=$currentPosition, duration=$duration, song=$song"
                )
                _isPlaying.postValue(isPlaying)
                _currentPosition.postValue(currentPosition.toInt())
                _duration.postValue(duration.toInt())
                _currentSong.postValue(song)
            }
        }
    }

    init {
        val filter = IntentFilter("PLAYER_STATE")
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onCleared() {
        super.onCleared()
        context.unregisterReceiver(receiver)
    }

    fun loadSongs(playlistType: String, playlistId: String? = null) {
        viewModelScope.launch {
            _songs.value = songRepository.getSongsBy(playlistType, playlistId)
        }
    }

    fun playSong(song: Song, index: Int) {
        songs.value?.let { SongManager.setSongList(it, index) }
        _currentSong.value = song
        serviceIntent.action = "ACTION_PLAY"
        context.startService(serviceIntent)
    }

    fun next() {
        serviceIntent.action = "ACTION_NEXT"
        context.startService(serviceIntent)

    }

    fun previous() {
        serviceIntent.action = "ACTION_PREVIOUS"
        context.startService(serviceIntent)
    }

    fun togglePlayPause() {
        serviceIntent.action = "TOGGLE_PLAY_PAUSE"
        context.startService(serviceIntent)
    }

    fun seekTo(position: Int) {
        serviceIntent.action = "ACTION_SEEK_TO"
        serviceIntent.putExtra("seekTo", position)
        context.startService(serviceIntent)
    }
}