@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.theme

import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleThemeConfiguration
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.RippleDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

enum class ThemeMode { LIGHT, DARK, SYSTEM, OLED }

fun applyThemeMode(themeMode: ThemeMode) {
    val nightMode = when (themeMode) {
        ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        ThemeMode.DARK, ThemeMode.OLED -> AppCompatDelegate.MODE_NIGHT_YES
        ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
    if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}

private val PureBlack = Color(0xFF000000)

@Composable
fun ShelfTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    accentColor: AccentColor = AccentColor.PERIWINKLE,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.OLED -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val isOled = themeMode == ThemeMode.OLED

    val context = LocalContext.current
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val dynamic = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        if (isOled) {
            val darkSurface = Color(0xFF0E0E0E)
            val lighterSurface = Color(0xFF161616)
            dynamic.copy(
                background = PureBlack,
                surface = PureBlack,
                surfaceDim = PureBlack,
                surfaceBright = darkSurface,
                surfaceContainerLowest = PureBlack,
                surfaceContainerLow = darkSurface,
                surfaceContainer = darkSurface,
                surfaceContainerHigh = lighterSurface,
                surfaceContainerHighest = lighterSurface,
            )
        } else {
            dynamic
        }
    } else {
        rememberDynamicColorScheme(
            seedColor = accentColor.seed,
            isDark = isDark,
            isAmoled = isOled,
            specVersion = if (isOled) ColorSpec.SpecVersion.SPEC_2021 else ColorSpec.SpecVersion.SPEC_2025
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowInsetsControllerCompat(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalRippleThemeConfiguration provides RippleDefaults.InsetFocusRingRippleThemeConfiguration
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = ShelfTypography,
            motionScheme = MotionScheme.expressive()
        ) {
            CompositionLocalProvider(
                LocalContentColor provides colorScheme.onSurface,
                content = content
            )
        }
    }
}
