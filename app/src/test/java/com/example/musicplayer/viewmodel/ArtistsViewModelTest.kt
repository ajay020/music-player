package com.example.musicplayer.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.musicplayer.data.model.Artist
import com.example.musicplayer.data.repository.ArtistsRepository
import com.example.musicplayer.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistsViewModelTest {
    private lateinit var artistsViewModel: ArtistsViewModel
    private var artistRepository = mock<ArtistsRepository>()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Set main dispatcher to test
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        artistsViewModel = ArtistsViewModel(artistRepository)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `loadArtists should update artists live data`() = runTest {
        // Arrange
        val dummyArtists = listOf(
            Artist(
                id = 1L,
                name = "Taylor swift",
                numberOfAlbums = 2,
                numberOfTracks = 10
            )
        )

        whenever(artistRepository.queryArtists()).thenReturn(dummyArtists)

        // Act
        artistsViewModel.loadArtists()
        advanceUntilIdle()

        //Assert
        val artists = artistsViewModel.artists.getOrAwaitValue()
        assertEquals(dummyArtists, artists)
    }
}