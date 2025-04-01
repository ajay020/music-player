package com.example.musicplayer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.musicplayer.adapter.MusicPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.viewPager)

        // Set up ViewPager with fragments
        val pagerAdapter = MusicPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "Songs"
                1 -> "Playlists"
                2 -> "Albums"
                3 -> "Artists"
                4 -> "Folders"
                5 -> "Genres"
                else -> null
            }
        }.attach()
    }
}