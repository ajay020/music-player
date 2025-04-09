package com.example.musicplayer.utils

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromList(list: List<Long>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toList(json: String): List<Long> {
        val type = object : TypeToken<List<Long>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }
}