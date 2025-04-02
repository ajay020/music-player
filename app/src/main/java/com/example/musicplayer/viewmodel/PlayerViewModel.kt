package com.example.musicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    private val _currentSong = MutableLiveData<Song>(null)
    val currentSong: LiveData<Song> = _currentSong

    // Current playlist information
    private var currentPlaylistType = "SONGS"
    private var currentPlaylistId: String? = null

    fun setCurrentSong(song: Song) {
        _currentSong.value = song
     }

    fun setCurrentPlaylistInfo( playlistType: String, playlistId: String? = null) {
        currentPlaylistType = playlistType
        currentPlaylistId = playlistId
    }

    fun getCurrentSong(): Song? {
        return _currentSong.value
    }

    fun getPreviousSong(): Song? {
        val current = _currentSong.value ?: return null
        return songRepository.getPreviousSong(current, currentPlaylistType, currentPlaylistId)
    }

    fun getNextSong(): Song? {
        val current = _currentSong.value ?: return null
        return songRepository.getNextSong(current, currentPlaylistType, currentPlaylistId)
    }

    fun loadSongs(playlistType: String, playlistId: String? = null) {

        // Load songs based on the playlist type and ID
        _songs.value = when (playlistType) {
            "SONGS" -> songRepository.getAllSongs()
            "ALBUM" -> songRepository.getSongsByAlbum(playlistId ?: "")
            "ARTIST" -> songRepository.getSongsByArtist(playlistId ?: "")
            "PLAYLIST" -> songRepository.getSongsByPlaylist(playlistId ?: "")
            else -> emptyList()
        }
    }

}