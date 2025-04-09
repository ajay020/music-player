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
    val defaultPlaylists: LiveData<List<Playlist>> = playlistRepository.getDefaultPlaylists().asLiveData()

    private val _playlistDetails = MutableLiveData<Playlist?>()
    val playlistDetails: LiveData<Playlist?> = _playlistDetails

    fun getPlaylistById(playlistId: Long) {
        viewModelScope.launch {
            _playlistDetails.value = playlistRepository.getPlaylistById(playlistId)
        }
    }

    fun insertPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepository.insertPlaylist(playlist)
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepository.updatePlaylist(playlist)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlist)
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

    // Function to create a new playlist
    fun createNewPlaylist(playlistName: String) {
        val newPlaylist = Playlist(name = playlistName)
        insertPlaylist(newPlaylist)
    }
}