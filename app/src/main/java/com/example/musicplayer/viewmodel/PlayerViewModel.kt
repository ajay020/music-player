package com.example.musicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val songRepository: MusicRepository
) : ViewModel() {

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    private val _currentSong = MutableLiveData<Song>(null)
    val currentSong: LiveData<Song> = _currentSong

    // Current playlist information
    private var currentPlaylistType = "SONGS"
    private var currentPlaylistId: String? = null


    private var currentSongIndex = 0
    var isPlayRequested = false

    init {
        viewModelScope.launch {
//            songRepository.loadSongs()
        }
    }

    fun loadSongs(playlistType: String, playlistId: String? = "") {
        viewModelScope.launch {
            val songsList = when (playlistType) {
                "SONGS" -> songRepository.getAllSongs()
//                "ALBUM" -> songRepository.getSongsByAlbum(playlistId ?: "")
//                "ARTIST" -> songRepository.getSongsByArtist(playlistId ?: "")
//                "PLAYLIST" -> songRepository.getSongsByPlaylist(playlistId ?: "")
                else -> emptyList()
            }
            _songs.value = songsList
        }
    }

    fun setCurrentSong(song: Song) {
        _currentSong.value = song

        // Update current index
        currentSongIndex = _songs.value?.indexOfFirst { it.id == song.id } ?: 0
        playCurrentSong()
    }

    fun playCurrentSong() {
        isPlayRequested = true
        _currentSong.value?.let {
            _currentSong.value = it // Trigger the observer
        }
    }

    fun setCurrentPlaylistInfo(playlistType: String, playlistId: String? = null) {
        currentPlaylistType = playlistType
        currentPlaylistId = playlistId
    }

    fun getCurrentSong(): Song? {
        return _currentSong.value
    }

    fun getNextSong(): Song? {
        val songsList = _songs.value ?: return null
        if (songsList.isEmpty()) return null

        currentSongIndex = (currentSongIndex + 1) % songsList.size
        return songsList[currentSongIndex].also {
            _currentSong.value = it
            isPlayRequested = true
        }
    }

    fun getPreviousSong(): Song? {
        val songsList = _songs.value ?: return null
        if (songsList.isEmpty()) return null

        currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1 else songsList.size - 1
        return songsList[currentSongIndex].also {
            _currentSong.value = it
            isPlayRequested = true
        }
    }

    fun playNextSong() {
        getNextSong()
    }

    fun playPreviousSong() {
        getPreviousSong()
    }

}