@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
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

private val PureBlack = Color(0xFF000000)

@Composable
fun ColorScheme.smoothTransition(durationMillis: Int = 250): ColorScheme {
    val spec: AnimationSpec<Color> = tween(durationMillis, easing = FastOutSlowInEasing)
    return this.copy(
        primary = animateColorAsState(primary, spec, label = "p").value,
        onPrimary = animateColorAsState(onPrimary, spec, label = "onP").value,
        primaryContainer = animateColorAsState(primaryContainer, spec, label = "pC").value,
        onPrimaryContainer = animateColorAsState(onPrimaryContainer, spec, label = "onPC").value,
        secondary = animateColorAsState(secondary, spec, label = "s").value,
        onSecondary = animateColorAsState(onSecondary, spec, label = "onS").value,
        secondaryContainer = animateColorAsState(secondaryContainer, spec, label = "sC").value,
        onSecondaryContainer = animateColorAsState(onSecondaryContainer, spec, label = "onSC").value,
        tertiary = animateColorAsState(tertiary, spec, label = "t").value,
        onTertiary = animateColorAsState(onTertiary, spec, label = "onT").value,
        tertiaryContainer = animateColorAsState(tertiaryContainer, spec, label = "tC").value,
        onTertiaryContainer = animateColorAsState(onTertiaryContainer, spec, label = "onTC").value,
        background = animateColorAsState(background, spec, label = "bg").value,
        onBackground = animateColorAsState(onBackground, spec, label = "onBg").value,
        surface = animateColorAsState(surface, spec, label = "surf").value,
        onSurface = animateColorAsState(onSurface, spec, label = "onSurf").value,
        surfaceVariant = animateColorAsState(surfaceVariant, spec, label = "surfVar").value,
        onSurfaceVariant = animateColorAsState(onSurfaceVariant, spec, label = "onSurfVar").value,
        surfaceTint = animateColorAsState(surfaceTint, spec, label = "surfTint").value,
        outline = animateColorAsState(outline, spec, label = "outl").value,
        outlineVariant = animateColorAsState(outlineVariant, spec, label = "outlVar").value,
        scrim = animateColorAsState(scrim, spec, label = "scrim").value,
        inverseSurface = animateColorAsState(inverseSurface, spec, label = "invSurf").value,
        inverseOnSurface = animateColorAsState(inverseOnSurface, spec, label = "invOnSurf").value,
        inversePrimary = animateColorAsState(inversePrimary, spec, label = "invP").value,
        surfaceDim = animateColorAsState(surfaceDim, spec, label = "surfDim").value,
        surfaceBright = animateColorAsState(surfaceBright, spec, label = "surfBri").value,
        surfaceContainerLowest = animateColorAsState(surfaceContainerLowest, spec, label = "sLow0").value,
        surfaceContainerLow = animateColorAsState(surfaceContainerLow, spec, label = "sLow").value,
        surfaceContainer = animateColorAsState(surfaceContainer, spec, label = "sCont").value,
        surfaceContainerHigh = animateColorAsState(surfaceContainerHigh, spec, label = "sHigh").value,
        surfaceContainerHighest = animateColorAsState(surfaceContainerHighest, spec, label = "sHigh1").value
    )
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
    val rawColorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

    val colorScheme = rawColorScheme.smoothTransition()

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
