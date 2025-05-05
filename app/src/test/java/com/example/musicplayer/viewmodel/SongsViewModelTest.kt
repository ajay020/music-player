package com.example.musicplayer.viewmodel

import android.net.Uri
import android.os.Parcelable
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repository.MusicRepository
import com.example.musicplayer.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.parcelize.Parcelize
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class SongsViewModelTest {
    private lateinit var viewModel: SongsViewModel
    private val fakeRepository = mock<MusicRepository>()
    private val mockUri = mock<Uri>()

    // Rule for LiveData
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Set main dispatcher to test
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SongsViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadSongs should update songs LiveData`() = runTest {
        // Arrange
        val dummySongs = listOf(
            Song(
                id = 1L,
                uri = mockUri,
                title = "Song One",
                artist = "Artist A",
                albumId = "101",
                duration = 180000L,
                path = "/storage/emulated/0/Music/song1.mp3"
            )
        )

        whenever(fakeRepository.getAllSongs()).thenReturn(dummySongs)

        // Act
        viewModel.loadSongs()
        advanceUntilIdle() // Wait for coroutine to finish

        // Assert
        val result = viewModel.songs.getOrAwaitValue()
        assertEquals(dummySongs, result)
    }

    @Test
    fun testCoroutineExecution() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)

        var result = 0

        launch(Dispatchers.Main) {
            delay(1000)
            result = 42
        }

        // At this point, coroutine hasn't run yet
        assertEquals(0, result)

        advanceUntilIdle() // Now all coroutines complete
        assertEquals(42, result)
    }
}