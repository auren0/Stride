package com.example.stride.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State

class ThemeViewModel : ViewModel() {
    private val _themeState = mutableStateOf(ThemeState(ThemeMode.SYSTEM))
    val themeState: State<ThemeState> = _themeState

    fun setThemeMode(themeMode: ThemeMode) {
        _themeState.value = ThemeState(themeMode)
    }
}

data class ThemeState(val themeMode: ThemeMode)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}