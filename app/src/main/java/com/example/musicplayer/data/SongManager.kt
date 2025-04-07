package com.example.musicplayer.data

import com.example.musicplayer.data.model.Song

object SongManager {
    var songList: List<Song> = emptyList()
    var currentIndex: Int = -1


    fun getCurrentSong(): Song? {
        return if (songList.isNotEmpty() && currentIndex in songList.indices) {
            songList[currentIndex]
        } else null
    }

    fun getNextSong(): Song? {
        if (songList.isEmpty()) return null
        currentIndex = (currentIndex + 1) % songList.size
        return songList[currentIndex]
    }

    fun getPreviousSong(): Song? {
        if (songList.isEmpty()) return null
        currentIndex = if (currentIndex - 1 < 0) songList.size - 1 else currentIndex - 1
        return songList[currentIndex]
    }

    fun setSongList(list: List<Song>, startIndex: Int) {
        songList = list
        currentIndex = startIndex
    }
}
