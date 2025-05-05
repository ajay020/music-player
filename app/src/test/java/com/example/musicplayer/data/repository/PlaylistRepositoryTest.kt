package com.example.musicplayer.data.repository

import com.example.musicplayer.data.database.PlaylistDao
import com.example.musicplayer.data.model.Playlist
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class PlaylistRepositoryTest {
    private lateinit var repository: PlaylistRepository
    private val playlistDao: PlaylistDao = mock()

    @Before
    fun setup() {
        repository = PlaylistRepository(playlistDao)
    }

    @Test
    fun `getAllPlaylists should return playlists from DAO`() = runTest {
        // Arrange
        val expectedPlaylists = listOf(
            Playlist(id = 1, name = "Rock"),
            Playlist(id = 2, name = "Pop")
        )

        whenever(playlistDao.getAllPlaylists()).thenReturn(flowOf(expectedPlaylists))
        val repository = PlaylistRepository(playlistDao)

        // Act
        val result = repository.getAllPlaylists().first()

        // Assert
        assertEquals(expectedPlaylists, result)
        verify(playlistDao).getAllPlaylists()
    }

    @Test
    fun `getPlaylistById should return playlist from DAO`() = runTest {
        // Arrange
        val playlistId = 1L
        val expectedPlaylist = Playlist(id = playlistId, name = "Chill Vibes")

        whenever(playlistDao.getPlaylistById(playlistId)).thenReturn(expectedPlaylist)
        val repository = PlaylistRepository(playlistDao)

        // Act
        val result = repository.getPlaylistById(playlistId)

        // Assert
        assertEquals(expectedPlaylist, result)
        verify(playlistDao).getPlaylistById(playlistId)
    }

    @Test
    fun `insertPlayList should insert playlist into DAO`() = runTest{
        // Arrange
        val playlistId = 1L
        val playlist = Playlist(id = 0, name = "New Playlist")
        whenever(playlistDao.insertPlaylist(playlist)).thenReturn(playlistId)

        // Act
        val result = repository.insertPlaylist(playlist)

        // Assert
        assertEquals(playlistId, result)
    }

    @Test
    fun `updatePlaylist should call DAO with correct playlist` () = runTest{
        // Arrange
        val playlist = Playlist(id = 1, name = "Updated Playlist")
        whenever(playlistDao.updatePlaylist(playlist)).thenReturn(Unit)

        // Act
        repository.updatePlaylist(playlist)

        // Assert
        verify(playlistDao).updatePlaylist(playlist)
    }

    @Test
    fun `deletePlaylist should call DAO with correct playlist`() = runTest {
        // Arrange
        val playlist = Playlist(id = 1, name = "To Delete")
        whenever(playlistDao.deletePlaylist(playlist)).thenReturn(Unit)

        // Act
        repository.deletePlaylist(playlist)

        // Assert
        verify(playlistDao).deletePlaylist(playlist)
    }

    @Test
    fun `getDefaultPlaylists should return flow from DAO`() = runTest {
        // Arrange
        val expectedPlaylists = listOf(
            Playlist(id = 1, name = "Favorites"),
            Playlist(id = 2, name = "Top 100")
        )
        whenever(playlistDao.getDefaultPlaylists()).thenReturn(flowOf(expectedPlaylists))

        // Act
        val result = repository.getDefaultPlaylists().first()

        // Assert
        assertEquals(expectedPlaylists, result)
        verify(playlistDao).getDefaultPlaylists()
    }

    @Test
    fun `addSongIdToPlaylist should fetch playlist, add songId, and update playlist`() = runTest {
        // Arrange
        val playlistId = 1L
        val songId = 42L
        val existingPlaylist = Playlist(
            id = playlistId,
            name = "My Playlist",
            songIds = listOf(1L, 2L),
            songCount = 2
        )

        whenever(playlistDao.getPlaylistById(playlistId)).thenReturn(existingPlaylist)

        // Act
        repository.addSongIdToPlaylist(playlistId, songId)

        // Assert
        val expectedUpdatedPlaylist = existingPlaylist.copy(
            songIds = listOf(1L, 2L, songId).distinct(),  // in case songId is already present
            songCount = 3
        )

        verify(playlistDao).getPlaylistById(playlistId)
        verify(playlistDao).updatePlaylist(expectedUpdatedPlaylist)
    }

    @Test
    fun `addSongIdToPlaylist should do nothing if playlist not found`() = runTest {
        // Arrange
        val playlistId = 99L
        val songId = 123L
        whenever(playlistDao.getPlaylistById(playlistId)).thenReturn(null)

        // Act
        repository.addSongIdToPlaylist(playlistId, songId)

        // Assert
        verify(playlistDao).getPlaylistById(playlistId)
        verify(playlistDao, never()).updatePlaylist(any())
    }

    @Test
    fun `removeSongIdFromPlaylist should remove song and update playlist`() = runTest {
        // Arrange
        val playlistId = 1L
        val songId = 101L
        val originalPlaylist = Playlist(
            id = playlistId,
            name = "My Playlist",
            songIds = listOf(101L, 102L, 103L),
            songCount = 3
        )

        val expectedUpdatedPlaylist = originalPlaylist.copy(
            songIds = listOf(102L, 103L),
            songCount = 2
        )

        whenever(playlistDao.getPlaylistById(playlistId)).thenReturn(originalPlaylist)

        // Act
        repository.removeSongIdFromPlaylist(playlistId, songId)

        // Assert
        verify(playlistDao).getPlaylistById(playlistId)
        verify(playlistDao).updatePlaylist(expectedUpdatedPlaylist)
    }

    @Test
    fun `removeSongIdFromPlaylist should do nothing if playlist not found`() = runTest {
        // Arrange
        val playlistId = 999L
        val songId = 101L
        whenever(playlistDao.getPlaylistById(playlistId)).thenReturn(null)

        // Act
        repository.removeSongIdFromPlaylist(playlistId, songId)

        // Assert
        verify(playlistDao).getPlaylistById(playlistId)
        verify(playlistDao, never()).updatePlaylist(any())
    }
}