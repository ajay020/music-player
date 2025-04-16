package com.example.musicplayer.adapter

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.musicplayer.ui.AlbumsFragment
import com.example.musicplayer.ui.ArtistsFragment
import com.example.musicplayer.ui.FoldersFragment
import com.example.musicplayer.ui.PlaylistsFragment
import com.example.musicplayer.ui.SongsFragment

// ViewPager adapter to handle fragments
class MusicPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments: SparseArray<Fragment> = SparseArray()

    override fun getItemCount(): Int = 5 // Total number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SongsFragment()
            1 -> PlaylistsFragment()
            2 -> AlbumsFragment()
            3 -> ArtistsFragment()
            4 -> FoldersFragment()
            else -> SongsFragment() // Default
        }.also {
            fragments.put(position, it)
        }
    }

    // Override this method to ensure fragments are saved and restored correctly
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        fragments.clear()
    }

    // Helper function to get the currently active fragment
    fun getCurrentFragment(position: Int): Fragment? {
        return fragments.get(position)
    }
}