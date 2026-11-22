@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.theme

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalRippleThemeConfiguration
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.RippleDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

enum class ThemeMode { LIGHT, DARK, SYSTEM, OLED }

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun applyThemeMode(themeMode: ThemeMode, context: Context? = null) {
}

private val PureBlack = Color(0xFF000000)

private val ExpressiveMotionScheme = MotionScheme.expressive()

@Composable
fun ThemeSystemBarSync(darkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(darkTheme) {
            val activity = view.context.findActivity()
            if (activity != null) {
                activity.enableEdgeToEdge(
                    statusBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
                val window = activity.window
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
            onDispose {}
        }
    }
}

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
        remember(isDark, isOled, context) {
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
        }
    } else {
        rememberDynamicColorScheme(
            seedColor = accentColor.seed,
            isDark = isDark,
            isAmoled = isOled,
            specVersion = if (isOled) ColorSpec.SpecVersion.SPEC_2021 else ColorSpec.SpecVersion.SPEC_2025
        )
    }

    ThemeSystemBarSync(darkTheme = isDark)

    CompositionLocalProvider(
        LocalRippleThemeConfiguration provides RippleDefaults.InsetFocusRingRippleThemeConfiguration
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = ShelfTypography,
            motionScheme = ExpressiveMotionScheme,
            content = content
        )
    }
}
