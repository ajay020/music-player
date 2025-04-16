package com.example.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.musicplayer.adapter.MusicPagerAdapter
import com.example.musicplayer.data.model.Searchable
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.viewmodel.MusicViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    private lateinit var pagerAdapter: MusicPagerAdapter

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Request necessary permissions
        requestNeededPermissions()

        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.viewPager)
        searchView = findViewById(R.id.search_view_main)

        setupSearchView()

        // Set up ViewPager with fragments
        pagerAdapter = MusicPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout with ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Songs"
                1 -> "Playlists"
                2 -> "Albums"
                3 -> "Artists"
                4 -> "Folders"
                else -> null
            }
        }.attach()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle submission (optional - you can perform search on text change)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Get the currently visible Fragment and pass the search query
                val currentFragment = pagerAdapter.getCurrentFragment(viewPager.currentItem)
                (currentFragment as? Searchable)?.onSearchQuery(newText)
                return true
            }
        })
    }


    private fun requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }
}