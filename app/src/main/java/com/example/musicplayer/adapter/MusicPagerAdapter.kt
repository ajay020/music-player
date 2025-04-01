package com.example.musicplayer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.musicplayer.ui.AlbumsFragment
import com.example.musicplayer.ui.ArtistsFragment
import com.example.musicplayer.ui.FoldersFragment
import com.example.musicplayer.ui.GenresFragment
import com.example.musicplayer.ui.PlaylistsFragment
import com.example.musicplayer.ui.SongsFragment

// ViewPager adapter to handle fragments
class MusicPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 6 // Total number of tabs

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> SongsFragment()
            1 -> PlaylistsFragment()
            2 -> AlbumsFragment()
            3 -> ArtistsFragment()
            4 -> FoldersFragment()
            5 -> GenresFragment()
            else -> SongsFragment() // Default
        }
    }
}