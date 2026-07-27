package `in`.iambhvsh.shelf.ui.theme

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

    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        rememberDynamicColorScheme(seedColor = accentColor.seed, isDark = isDark)
    }

    val finalColorScheme = if (themeMode == ThemeMode.OLED) {
        val darkSurface = Color(0xFF0A0A0A)
        val lighterSurface = Color(0xFF121212)
        colorScheme.copy(
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
