package com.example.musicplayer.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.BuildConfig
import com.example.musicplayer.R


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Add back button to toolbar
        supportActionBar?.title = "Settings"

        // Get references to the setting items
        val themeSetting = findViewById<LinearLayout>(R.id.theme_setting)
        val aboutAppSetting = findViewById<LinearLayout>(R.id.about_app_setting)
        val privacyPolicySetting = findViewById<LinearLayout>(R.id.privacy_policy_setting)
        val versionSetting = findViewById<LinearLayout>(R.id.version_setting)
        val appVersionTextView = findViewById<TextView>(R.id.app_version)
        val currentThemeTextView = findViewById<TextView>(R.id.current_theme)

        // Load the current theme and update the TextView
        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val currentTheme = sharedPreferences.getString("theme", "light") ?: "light"
        currentThemeTextView.text = when (currentTheme) {
            "light" -> "Light"
            "dark" -> "Dark"
            "system" -> "System Default"
            else -> "Light"
        }

        // Set the app version
        appVersionTextView.text = BuildConfig.VERSION_NAME

        // Handle clicks on the settings items
        themeSetting.setOnClickListener {
            val themeDialogFragment = ThemeDialogFragment()
            themeDialogFragment.show(supportFragmentManager, ThemeDialogFragment.TAG)
        }

        aboutAppSetting.setOnClickListener {
            // Implement "About App" screen or dialog here
            // You could display app name, developer info, etc.
        }

        privacyPolicySetting.setOnClickListener {
            // Open the privacy policy URL or display it in an activity/dialog
            val privacyPolicyUrl = "YOUR_PRIVACY_POLICY_URL_HERE" // Replace with your actual URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
            startActivity(intent)
        }

        versionSetting.setOnClickListener {
            // The version is already displayed, you might add more info if needed
            // For example, build number
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}