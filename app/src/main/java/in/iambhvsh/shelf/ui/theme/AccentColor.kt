package in.iambhvsh.shelf.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Manual accent (seed) colors offered in the "Accent color" bottom sheet,
 * used the same way Android's Material You picks a seed color from a
 * wallpaper — here the user just picks the seed directly.
 * Values are Google's official Material Design baseline palette (500 tones).
 *
 * Stored by ordinal — always append new entries at the end, never reorder,
 * so existing users' saved selections don't silently change.
 */
enum class AccentColor(val label: String, val seed: Color) {
    RED("Red", Color(0xFFF44336)),
    ORANGE("Orange", Color(0xFFFF9800)),
    YELLOW("Yellow", Color(0xFFFFEB3B)),
    GREEN("Green", Color(0xFF4CAF50)),
    TEAL("Teal", Color(0xFF009688)),
    BLUE("Blue", Color(0xFF2196F3)),
    PURPLE("Purple", Color(0xFF9C27B0)),
}
