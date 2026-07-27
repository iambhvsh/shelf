package com.iambhvsh.shelf.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.rememberDynamicColorScheme

// OLED appended at the end (not inserted after DARK) so existing saved
// preferences, which are stored by ordinal, aren't silently remapped.
enum class ThemeMode { LIGHT, DARK, SYSTEM, OLED }

private val PureBlack = Color(0xFF000000)

@Composable
fun ShelfTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    accentColor: AccentColor = AccentColor.BLUE,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.OLED -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Same idea as Android's Material You: the whole scheme (primary,
    // secondary, tertiary, surfaces, everything) is derived from one seed
    // color. On Android 12+ with Dynamic color on, that seed comes from the
    // wallpaper. Otherwise it comes from the user's manual Accent color pick.
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        rememberDynamicColorScheme(seedColor = accentColor.seed, isDark = isDark)
    }

    // Force every surface token to pure black for the Oled theme, so
    // backgrounds, cards, sheets, and nav bars all render as true black,
    // while accent/primary colors from the scheme above are kept intact.
    val finalColorScheme = if (themeMode == ThemeMode.OLED) {
        colorScheme.copy(
            background = PureBlack,
            surface = PureBlack,
            surfaceDim = PureBlack,
            surfaceBright = PureBlack,
            surfaceContainerLowest = PureBlack,
            surfaceContainerLow = PureBlack,
            surfaceContainer = PureBlack,
            surfaceContainerHigh = PureBlack,
            surfaceContainerHighest = PureBlack,
        )
    } else {
        colorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            WindowInsetsControllerCompat(
                (view.context as Activity).window, view
            ).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = Typography,
        content = content
    )
}
