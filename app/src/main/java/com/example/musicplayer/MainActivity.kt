package com.example.musicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.musicplayer.adapter.MusicPagerAdapter
import com.example.musicplayer.data.model.Searchable
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.viewmodel.MusicViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.drawable.toDrawable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentContainerView
import com.example.musicplayer.data.model.Sortable
import com.example.musicplayer.ui.SettingsActivity
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.common.io.Resources
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.musicplayer.utils.Helper

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    private lateinit var pagerAdapter: MusicPagerAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var miniPlayerContainer: FragmentContainerView

    private val musicViewModel: MusicViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        applySavedTheme()
        DynamicColors.applyToActivityIfAvailable(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Request necessary permissions
        requestNeededPermissions()

        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.viewPager)
        searchView = findViewById(R.id.search_view_main)
        toolbar = findViewById(R.id.toolbar_main)
        miniPlayerContainer = findViewById(R.id.mini_player_fragment)


        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

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

        observeCurrentSong()
    }

    private fun observeCurrentSong() {
        musicViewModel.currentSong.observe(this) { selectedSong ->
            if (selectedSong != null) {
                miniPlayerContainer.visibility = View.VISIBLE
            } else {
                miniPlayerContainer.visibility = View.GONE
            }
        }
    }

    private fun applySavedTheme() {
        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedTheme = sharedPreferences.getString("theme", "light") ?: "light"

        when (savedTheme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)

        // Get your icon item
        val menuItem = menu.findItem(R.id.action_more)
        val drawable = menuItem.icon

        // Get the themed color for icon
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.colorControlNormal, typedValue, true)
        val iconColor = ContextCompat.getColor(this, typedValue.resourceId)

        drawable?.setTint(iconColor)
        menuItem.icon = drawable

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_more -> {
                showMoreOptionsDialog()
                true
            }
            // Handle other menu items if you have them
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun showMoreOptionsDialog() {
        val dialog = Dialog(this, R.style.ThemeOverlay_MusicPlayer_Dialog)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_more_options, null)

        dialog.setContentView(view)

        // Customize the dialog's window
        val window = dialog.window
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setGravity(Gravity.CENTER)

        // Set fixed width and height
        val fixedWidthInPixels = resources.getDimensionPixelSize(R.dimen.more_options_dialog_width)
        val fixedHeightInPixels =
            resources.getDimensionPixelSize(R.dimen.more_options_dialog_height)
        window?.setLayout(fixedWidthInPixels, fixedHeightInPixels)

        // Optional: Disable outside touch to cancel
        dialog.setCanceledOnTouchOutside(true)

        dialog.show()

        // Click listeners
        val sortByOption = view.findViewById<LinearLayout>(R.id.option_sort_by)
        val equalizerOption = view.findViewById<LinearLayout>(R.id.option_equalizer)
        val settingsOption = view.findViewById<LinearLayout>(R.id.option_settings)

        sortByOption?.setOnClickListener {
            showSortByDialog()
            dialog.dismiss()
        }

        equalizerOption?.setOnClickListener {
            Toast.makeText(this, "Equalizer", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        settingsOption?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }
    }

    private fun showSortByDialog() {
        val currentFragment =
            (viewPager.adapter as? MusicPagerAdapter)?.getCurrentFragment(viewPager.currentItem)

        if (currentFragment is Sortable) {
            val sortOptions = currentFragment.getSortOptions().toTypedArray()
            val builder = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MusicPlayer_Dialog)
            builder.setTitle("Sort By")

            var selectedSortOption = ""

            builder.setSingleChoiceItems(sortOptions, -1) { _, which ->
                selectedSortOption = sortOptions[which]
            }

            builder.setPositiveButton("OK") { dialog, _ ->
                if (selectedSortOption.isNotEmpty()) {
                    Toast.makeText(this, "Sorted by: $selectedSortOption", Toast.LENGTH_SHORT)
                        .show()
                    (currentFragment as? Sortable)?.onSortBy(selectedSortOption)
                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        } else {
            // Handle the case where the current fragment doesn't implement Sortable
            Toast.makeText(this, "Sorting not available for this section", Toast.LENGTH_SHORT)
                .show()
        }
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
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ),
                    100
                )
            }
        }
    }
}