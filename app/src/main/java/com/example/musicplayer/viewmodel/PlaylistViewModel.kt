package com.example.musicplayer.viewmodel

import androidx.lifecycle.*
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    val allPlaylists: LiveData<List<Playlist>> = playlistRepository.getAllPlaylists().asLiveData()
    val defaultPlaylists: LiveData<List<Playlist>> =
        playlistRepository.getDefaultPlaylists().asLiveData()

    private val _playlistDetails = MutableLiveData<Playlist?>()
    val playlistDetails: LiveData<Playlist?> = _playlistDetails

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun getPlaylistById(playlistId: Long) {
        viewModelScope.launch {
            try {
                _playlistDetails.value = playlistRepository.getPlaylistById(playlistId)
            } catch (e: Exception) {
                _error.value = "Failed to load playlist"
            }
        }
    }

    fun insertPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                playlistRepository.insertPlaylist(playlist)
            } catch (e: Exception) {
                _error.value = "Failed to insert playlist"
            }
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                playlistRepository.updatePlaylist(playlist)
            } catch (e: Exception) {
                _error.value = "Failed to update"
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                playlistRepository.deletePlaylist(playlist)
            } catch (e: Exception) {
                _error.value = "Failed to delete playlist"
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongIdToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.removeSongIdFromPlaylist(playlistId, songId)
        }
    }

}