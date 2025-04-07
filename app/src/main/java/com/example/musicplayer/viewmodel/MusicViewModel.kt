package com.example.musicplayer.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
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
    private val _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> = _currentSong

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    private val serviceIntent = Intent(context, MusicService::class.java)

    fun loadSongs(playlistType: String, playlistId: String? = null) {
        viewModelScope.launch {
            _songs.value = songRepository.getSongsBy(playlistType)
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

}