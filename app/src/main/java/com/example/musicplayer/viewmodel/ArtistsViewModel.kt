package com.example.musicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.model.Artist
import com.example.musicplayer.data.repository.ArtistsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    private val artistsRepository: ArtistsRepository
): ViewModel() {
    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> = _artists

    init {
        loadArtists()
    }

    private fun loadArtists() {
        viewModelScope.launch(Dispatchers.IO) {
            val artistList = artistsRepository.queryArtists()
            _artists.postValue(artistList)
        }
    }
}