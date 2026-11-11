package com.example.stride.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

fun ColorScheme.toOledColorScheme(): ColorScheme = copy(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF080808),
    surfaceContainer = Color(0xFF101010),
    surfaceContainerHigh = Color(0xFF181818),
    surfaceContainerHighest = Color(0xFF222222),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

fun getColorScheme(
    context: Context,
    themeMode: ThemeMode,
    accentColor: AccentColor
): ColorScheme {
    val isSystem = accentColor == AccentColor.SYSTEM
    val canUseDynamic = isSystem && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    return when (themeMode) {
        ThemeMode.LIGHT -> {
            if (canUseDynamic) {
                dynamicLightColorScheme(context)
            } else {
                when (accentColor) {
                    AccentColor.BLUE -> BlueLightScheme
                    AccentColor.GREEN -> GreenLightScheme
                    AccentColor.ORANGE -> OrangeLightScheme
                    AccentColor.RED -> RedLightScheme
                    AccentColor.TEAL -> TealLightScheme
                    AccentColor.PURPLE, AccentColor.SYSTEM -> PurpleLightScheme
                }
            }
        }
        ThemeMode.DARK -> {
            if (canUseDynamic) {
                dynamicDarkColorScheme(context)
            } else {
                when (accentColor) {
                    AccentColor.BLUE -> BlueDarkScheme
                    AccentColor.GREEN -> GreenDarkScheme
                    AccentColor.ORANGE -> OrangeDarkScheme
                    AccentColor.RED -> RedDarkScheme
                    AccentColor.TEAL -> TealDarkScheme
                    AccentColor.PURPLE, AccentColor.SYSTEM -> PurpleDarkScheme
                }
            }
        }
        ThemeMode.OLED -> {
            val baseDarkScheme = if (canUseDynamic) {
                dynamicDarkColorScheme(context)
            } else {
                when (accentColor) {
                    AccentColor.BLUE -> BlueDarkScheme
                    AccentColor.GREEN -> GreenDarkScheme
                    AccentColor.ORANGE -> OrangeDarkScheme
                    AccentColor.RED -> RedDarkScheme
                    AccentColor.TEAL -> TealDarkScheme
                    AccentColor.PURPLE, AccentColor.SYSTEM -> PurpleDarkScheme
                }
            }
            baseDarkScheme.toOledColorScheme()
        }
    }
}

@Composable
fun StrideTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentColor: AccentColor = AccentColor.SYSTEM,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = themeMode == ThemeMode.DARK || themeMode == ThemeMode.OLED
    val colorScheme = getColorScheme(context, themeMode, accentColor)

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
