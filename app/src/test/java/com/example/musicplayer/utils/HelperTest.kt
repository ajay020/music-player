package com.example.musicplayer.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class HelperTest {

    @Test
    fun testFormatTime_withMinutesAndSeconds() {
        val input = 125000L // 2 minutes, 5 seconds
        val result = Helper.formatTime(input)
        assertEquals("02:05", result)
    }

    @Test
    fun testFormatTime_withHoursMinutesSeconds() {
        val input = 3_900_000L // 1 hour, 5 minutes
        val result = Helper.formatTime(input)
        assertEquals("01:05:00", result)
    }

    @Test
    fun testFormatTime_withZero() {
        val input = 0L
        val result = Helper.formatTime(input)
        assertEquals("00:00", result)
    }

    @Test
    fun testGetAlbumArtUri() {
        val albumId = 12345L
        val uri = Helper.getAlbumArtUri(albumId)
        assertEquals("content://media/external/audio/albumart/12345", uri.toString())
    }
}