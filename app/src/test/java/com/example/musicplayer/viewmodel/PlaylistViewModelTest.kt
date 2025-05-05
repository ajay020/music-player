package com.example.musicplayer.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.musicplayer.base.BaseViewModelTest
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.repository.PlaylistRepository
import com.example.musicplayer.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PlaylistViewModelTest {
    private lateinit var viewModel: PlaylistViewModel
    private var playlistRepository: PlaylistRepository = mock()

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        whenever(playlistRepository.getAllPlaylists()).thenReturn(flowOf(emptyList()))
        whenever(playlistRepository.getDefaultPlaylists()).thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `get all playlists`() = runTest {
        // Arrange
        val playlists = listOf(
            Playlist(
                id = 1,
                name = "Playlist 1"
            ),
            Playlist(
                id = 2,
                name = "Playlist 2"
            )
        )

//        // 🔥 Stub BEFORE ViewModel is created
        whenever(playlistRepository.getAllPlaylists()).thenReturn(flowOf(playlists))
        whenever(playlistRepository.getDefaultPlaylists()).thenReturn(flowOf(emptyList()))

        // Now create the ViewModel
        viewModel = PlaylistViewModel(playlistRepository)

        // Act
        val result = viewModel.allPlaylists.getOrAwaitValue()
        val defaultResult = viewModel.defaultPlaylists.getOrAwaitValue()

        // Assert
        assertEquals(playlists, result)
        assertEquals(defaultResult, emptyList<Playlist>())
    }

    @Test
    fun `getPlaylistById should update playlistDetails LiveData`() = runTest {
        // Arrange
        val playlistId = 1L
        val dummyPlaylist = Playlist(id = playlistId, name = "Chill Vibes")


        whenever(playlistRepository.getPlaylistById(playlistId)).thenReturn(dummyPlaylist)

        viewModel = PlaylistViewModel(playlistRepository)

        // Act
        viewModel.getPlaylistById(playlistId)
        advanceUntilIdle() // let coroutines complete

        // Assert
        val result = viewModel.playlistDetails.getOrAwaitValue()
        assertEquals(dummyPlaylist, result)
    }

    @Test
    fun `getPlaylistById should update playlistDetails with null when repository returns null`() =
        runTest {
            // Arrange
            val playlistId = 99L
            whenever(playlistRepository.getPlaylistById(playlistId)).thenReturn(null)

            viewModel = PlaylistViewModel(playlistRepository)

            // Act
            viewModel.getPlaylistById(playlistId)
            advanceUntilIdle()

            // Assert
            val result = viewModel.playlistDetails.getOrAwaitValue()
            assertNull(result)
        }

    @Test
    fun `getPlaylistById should not crash when repository throws exception`() = runTest {
        // Arrange
        val playlistId = 1L
        whenever(playlistRepository.getPlaylistById(playlistId)).thenThrow(RuntimeException("Database error"))

        viewModel = PlaylistViewModel(playlistRepository)

        // Act
        viewModel.getPlaylistById(playlistId)
        advanceUntilIdle()

        // Assert
        // Since the exception is thrown, LiveData should remain null (default state)
        val result = viewModel.playlistDetails.value
        assertNull(result)
    }

    @Test
    fun `insertPlaylist should call repository and not set error on success`() = runTest {
        // Arrange
        val playlist = Playlist(id = 0, name = "New Playlist")
        whenever(playlistRepository.insertPlaylist(playlist)).thenReturn(1L)

        viewModel = PlaylistViewModel(playlistRepository)

        // Act
        viewModel.insertPlaylist(playlist)
        advanceUntilIdle()

        // Assert
        verify(playlistRepository).insertPlaylist(playlist)
        val error = viewModel.error.value
        assertNull(error)
    }

    @Test
    fun `insertPlaylist should set error when repository throws exception`() = runTest {
        // Arrange
        val playlist = Playlist(id = 0, name = "New Playlist")
        whenever(playlistRepository.insertPlaylist(playlist)).thenThrow(RuntimeException("DB insert failed"))

        viewModel = PlaylistViewModel(playlistRepository)

        // Act
        viewModel.insertPlaylist(playlist)
        advanceUntilIdle()

        // Assert
        val error = viewModel.error.getOrAwaitValue()
        assertEquals("Failed to insert playlist", error)
    }

    @Test
    fun `updatePlaylist should call repository and not set error on success`() = runTest {
        // Arrange
        val playlist = Playlist(id = 1, name = "Updated Playlist")
        whenever(playlistRepository.updatePlaylist(playlist)).thenReturn(Unit)

        viewModel = PlaylistViewModel(playlistRepository)

        // Act
        viewModel.updatePlaylist(playlist)
        advanceUntilIdle()

        // Assert
        verify(playlistRepository).updatePlaylist(playlist)
        val error = viewModel.error.value
        assertNull(error)
    }

    @Test
    fun `updatePlaylist should set error when repository throws exception`() = runTest {
        // Arrange
        val playlist = Playlist(id = 1, name = "Updated Playlist")
        whenever(playlistRepository.updatePlaylist(playlist)).thenThrow(RuntimeException("Update failed"))

        viewModel = PlaylistViewModel(playlistRepository)

        // Act
        viewModel.updatePlaylist(playlist)
        advanceUntilIdle()

        // Assert
        val error = viewModel.error.getOrAwaitValue()
        assertEquals("Failed to update", error)
    }

    @Test
    fun `deletePlaylist should call repository and not set error on success`() = runTest {
        // Arrange
        val playlist = Playlist(id = 1, name = "My Playlist")
        whenever(playlistRepository.deletePlaylist(playlist)).thenReturn(Unit)

        viewModel = PlaylistViewModel(playlistRepository)

        // Act
        viewModel.deletePlaylist(playlist)
        advanceUntilIdle()

        // Assert
        verify(playlistRepository).deletePlaylist(playlist)
        val error = viewModel.error.value
        assertNull(error)
    }

    @Test
    fun `deletePlaylist should set error when repository throws exception`() = runTest {
        // Arrange
        val playlist = Playlist(id = 1, name = "My Playlist")
        whenever(playlistRepository.deletePlaylist(playlist)).thenThrow(RuntimeException("DB Error"))

        viewModel = PlaylistViewModel(playlistRepository)

        // Act
        viewModel.deletePlaylist(playlist)
        advanceUntilIdle()

        // Assert
        verify(playlistRepository).deletePlaylist(playlist)
        val error = viewModel.error.value
        assertEquals("Failed to delete playlist", error)
    }

    @Test
    fun `addSongToPlaylist should call repository with correct arguments`() = runTest {
        // Arrange
        val playlistId = 42L
        val songId = 99L
        viewModel = PlaylistViewModel(playlistRepository)

        // Act
        viewModel.addSongToPlaylist(playlistId, songId)
        advanceUntilIdle()

        // Assert
        verify(playlistRepository).addSongIdToPlaylist(playlistId, songId)
    }
}