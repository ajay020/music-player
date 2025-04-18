package com.example.musicplayer.ui

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import androidx.core.content.edit

class ThemeDialogFragment : DialogFragment() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        sharedPreferences =
            requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val currentTheme = sharedPreferences.getString("theme", "light") ?: "light"

        val themeOptions = arrayOf("Light", "Dark", "System Default")
        val checkedItem = when (currentTheme) {
            "light" -> 0
            "dark" -> 1
            "system" -> 2
            else -> 0
        }

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themeOptions, checkedItem) { _, which ->
                val selectedTheme = when (which) {
                    0 -> "light"
                    1 -> "dark"
                    2 -> "system"
                    else -> "light"
                }
                saveTheme(selectedTheme)
                applyTheme(selectedTheme)
                dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }

    private fun saveTheme(theme: String) {
        sharedPreferences.edit { putString("theme", theme) }
    }

    private fun applyTheme(theme: String) {
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    companion object {
        const val TAG = "ThemeDialogFragment"
    }
}