package com.example.musicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.model.Album
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repository.AlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val albumRepository: AlbumRepository
) : ViewModel() {

    private val _albums = MutableLiveData<List<Album>>(emptyList())
    val albums = _albums

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    fun loadAlbums() {
        viewModelScope.launch {
            _albums.value = albumRepository.loadAlbums()
        }
    }

    fun loadSongsByAlbum(albumId: Long) {
        viewModelScope.launch {
            _songs.value = albumRepository.loadSongsByAlbum(albumId)
        }
    }

    fun getAlbumById(albumId: Long): Album? {
        return albums.value?.find { it.id == albumId }
    }
}