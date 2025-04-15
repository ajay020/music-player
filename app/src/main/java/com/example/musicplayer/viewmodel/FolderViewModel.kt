package com.example.musicplayer.viewmodel

import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.model.FolderWithSongCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val applicationContext: Context
) : ViewModel() {
    private val _folderWithSongCount = MutableLiveData<List<FolderWithSongCount>>()
    val folderWithSongCount: LiveData<List<FolderWithSongCount>> = _folderWithSongCount

    init {
        loadFoldersWithSongCount()
    }

    private fun loadFoldersWithSongCount() {
        viewModelScope.launch {
            val folders = withContext(Dispatchers.IO) {
                getFoldersAndSongCount(applicationContext)
            }
            _folderWithSongCount.value = folders
        }
    }

    private fun getFoldersAndSongCount(context: Context): List<FolderWithSongCount> {
        val folderMap = mutableMapOf<String, Int>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            while (it.moveToNext()) {
                val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val folder = path.substringBeforeLast("/")
                folderMap[folder] = (folderMap[folder] ?: 0) + 1
            }
        }

        return folderMap.map { (folderName, count) ->
            val simpleFolderName = folderName.substringAfterLast("/")
            FolderWithSongCount(simpleFolderName, count)
        }.sortedBy { it.folderName }
    }
}