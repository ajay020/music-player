package com.example.musicplayer.data.model

interface Sortable {
    fun onSortBy(sortBy: String)
    fun getSortOptions(): List<String>
}