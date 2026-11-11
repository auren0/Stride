package com.example.stride

import com.example.stride.ui.theme.AccentColor
import com.example.stride.ui.theme.BlueDarkScheme
import com.example.stride.ui.theme.BlueLightScheme
import com.example.stride.ui.theme.GreenDarkScheme
import com.example.stride.ui.theme.GreenLightScheme
import com.example.stride.ui.theme.OrangeDarkScheme
import com.example.stride.ui.theme.OrangeLightScheme
import com.example.stride.ui.theme.PurpleDarkScheme
import com.example.stride.ui.theme.PurpleLightScheme
import com.example.stride.ui.theme.RedDarkScheme
import com.example.stride.ui.theme.RedLightScheme
import com.example.stride.ui.theme.TealDarkScheme
import com.example.stride.ui.theme.TealLightScheme
import com.example.stride.ui.theme.ThemeMode
import com.example.stride.ui.theme.toOledColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemeColorTest {

    @Test
    fun allAccentColorsHaveTitlesAndPreviews() {
        AccentColor.entries.forEach { accent ->
            assertNotNull(accent.title)
            assertNotNull(accent.previewColor)
        }
    }

    @Test
    fun oledSchemeOverridesBackgroundAndSurface() {
        val oledPurple = PurpleDarkScheme.toOledColorScheme()
        assertEquals(androidx.compose.ui.graphics.Color(0xFF000000), oledPurple.background)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF000000), oledPurple.surface)
        assertEquals(PurpleDarkScheme.primary, oledPurple.primary)
    }

    @Test
    fun presetSchemesHaveValidPrimaryColors() {
        val schemes = listOf(
            PurpleLightScheme to PurpleDarkScheme,
            BlueLightScheme to BlueDarkScheme,
            GreenLightScheme to GreenDarkScheme,
            OrangeLightScheme to OrangeDarkScheme,
            RedLightScheme to RedDarkScheme,
            TealLightScheme to TealDarkScheme
        )

        schemes.forEach { (light, dark) ->
            assertNotNull(light.primary)
            assertNotNull(light.primaryContainer)
            assertNotNull(dark.primary)
            assertNotNull(dark.primaryContainer)
        }
    }
}
