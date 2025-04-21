package com.example.musicplayer.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.musicplayer.MainActivity
import com.example.musicplayer.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        // Apply night mode settings from preferences
        applySavedTheme()

        // Add a small delay to show the splash screen briefly
        Handler(Looper.getMainLooper()).postDelayed({
            // Start MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Close this activity
        }, 1000) // 500ms delay
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
}