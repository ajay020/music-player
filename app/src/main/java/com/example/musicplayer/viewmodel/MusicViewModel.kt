package com.example.musicplayer.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.SongManager
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repository.MusicRepository
import com.example.musicplayer.service.MusicService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicRepository: MusicRepository,
    val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val playlistId = savedStateHandle.get<Long>("PLAYLIST_ID")
    private val artistId = savedStateHandle.get<Long>("ARTIST_ID")
    private val albumId = savedStateHandle.get<Long>("ALBUM_ID")
    private val folderName = savedStateHandle.get<String>("FOLDER_NAME")
    private val type = savedStateHandle.get<String>("TYPE")

    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> = _currentSong.distinctUntilChanged()

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

                _isPlaying.postValue(isPlaying)
                _currentPosition.postValue(currentPosition.toInt())
                _duration.postValue(duration.toInt())
                updateCurrentSong(song)
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

        loadSongsBasedOnType()
    }

    private fun loadSongsBasedOnType() {
        viewModelScope.launch {
            when (type) {
                "SONGS" -> musicRepository.getSongs("SONGS")
                "PLAYLIST" -> playlistId?.let { loadSongsBasedOnType("PLAYLIST", it) }
                "ARTIST" -> artistId?.let { loadSongsBasedOnType("ARTIST", it) }
                "ALBUM" -> albumId?.let { loadSongsBasedOnType("ALBUM", it) }
                "FOLDER" -> folderName?.let { loadSongsBasedOnType("FOLDER", folderName = it) }
                else -> {
                    Log.e("MusicViewModel", "Unknown type: $type")
                    _songs.value = emptyList()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        context.unregisterReceiver(receiver)
    }

    fun loadSongsBasedOnType(
        playlistType: String,
        playlistId: Long? = null,
        folderName: String? = null
    ) {
        viewModelScope.launch {
            _songs.value = musicRepository.getSongs(
                type = playlistType,
                id = playlistId,
                folderName = folderName
            )
        }
    }

    private fun updateCurrentSong(song: Song?) {
        if (_currentSong.value?.id != song?.id) {
            _currentSong.postValue(song)
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