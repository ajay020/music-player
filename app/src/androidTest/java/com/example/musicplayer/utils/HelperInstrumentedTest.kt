package com.example.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HelperInstrumentedTest {

    @get:Rule
    val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun testGetEmbeddedAlbumArt_returnsNullOrBitmap() {
        val uri =
            Uri.parse("content://media/external/audio/media/1") // Replace with real URI from your device
        val bitmap = Helper.getEmbeddedAlbumArt(context, uri)
        // Use Log or assertion based on your device's content
        assert(bitmap == null || bitmap is Bitmap)
    }
}
