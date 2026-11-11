package com.example.stride.ui.theme

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel

enum class ThemeMode {
    LIGHT, DARK, OLED
}

enum class AccentColor(val title: String, val previewColor: Color) {
    SYSTEM("Системный", Color(0xFF6750A4)),
    PURPLE("Фиолетовый", Color(0xFF6750A4)),
    BLUE("Синий", Color(0xFF0061A4)),
    GREEN("Изумрудный", Color(0xFF1B6D24)),
    ORANGE("Оранжевый", Color(0xFF964900)),
    RED("Коралловый", Color(0xFFB32638)),
    TEAL("Морской", Color(0xFF006A67))
}

data class ThemeState(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentColor: AccentColor = AccentColor.SYSTEM
)

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val initialTheme: ThemeMode = runCatching {
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
    }.getOrDefault(ThemeMode.DARK)

    private val initialAccent: AccentColor = runCatching {
        AccentColor.valueOf(prefs.getString("accent_color", AccentColor.SYSTEM.name) ?: AccentColor.SYSTEM.name)
    }.getOrDefault(AccentColor.SYSTEM)

    private val _themeState = mutableStateOf(ThemeState(themeMode = initialTheme, accentColor = initialAccent))
    val themeState: State<ThemeState> = _themeState

    fun setThemeMode(themeMode: ThemeMode) {
        prefs.edit().putString("theme_mode", themeMode.name).apply()
        _themeState.value = _themeState.value.copy(themeMode = themeMode)
    }

    fun setAccentColor(accentColor: AccentColor) {
        prefs.edit().putString("accent_color", accentColor.name).apply()
        _themeState.value = _themeState.value.copy(accentColor = accentColor)
    }
}
