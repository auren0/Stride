package com.example.stride.ui.theme

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

enum class ThemeMode {
    LIGHT, DARK, OLED
}

data class ThemeState(val themeMode: ThemeMode)

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val initialTheme: ThemeMode = runCatching {
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
    }.getOrDefault(ThemeMode.DARK)

    private val _themeState = mutableStateOf(ThemeState(initialTheme))
    val themeState: State<ThemeState> = _themeState

    fun setThemeMode(themeMode: ThemeMode) {
        prefs.edit().putString("theme_mode", themeMode.name).apply()
        _themeState.value = ThemeState(themeMode)
    }
}
