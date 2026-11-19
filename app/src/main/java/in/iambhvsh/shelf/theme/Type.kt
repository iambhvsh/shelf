package `in`.iambhvsh.shelf.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import `in`.iambhvsh.shelf.R

private const val Features = "ss02, dlig"

val Flex400 = FontFamily(
    Font(
        R.font.google_sans_flex,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    )
)

val Flex500 = FontFamily(
    Font(
        R.font.google_sans_flex,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),
            FontVariation.Setting("ROND", 50f)
        )
    )
)

val Flex600 = FontFamily(
    Font(
        R.font.google_sans_flex,
        FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(600),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

val HeroFont = FontFamily(
    Font(
        R.font.google_sans_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(900),
            FontVariation.width(112.5f),
            FontVariation.Setting("ROND", 35f)
        )
    )
)

val InlineFont = FontFamily(
    Font(
        R.font.google_sans_flex,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.google_sans_flex,
        FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(600),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

private val Base = Typography()

val ShelfTypography = Typography(
    displayLarge = Base.displayLarge.copy(fontFamily = Flex600, fontFeatureSettings = Features),
    displayMedium = Base.displayMedium.copy(fontFamily = Flex600, fontFeatureSettings = Features),
    displaySmall = Base.displaySmall.copy(fontFamily = Flex600, fontFeatureSettings = Features),
    headlineLarge = Base.headlineLarge.copy(fontFamily = Flex600, fontFeatureSettings = Features),
    headlineMedium = Base.headlineMedium.copy(fontFamily = Flex600, fontFeatureSettings = Features),
    headlineSmall = Base.headlineSmall.copy(fontFamily = Flex600, fontFeatureSettings = Features),
    titleLarge = Base.titleLarge.copy(fontFamily = Flex400, fontFeatureSettings = Features),
    titleMedium = Base.titleMedium.copy(fontFamily = Flex600, fontFeatureSettings = Features),
    titleSmall = Base.titleSmall.copy(fontFamily = Flex600, fontFeatureSettings = Features),
    bodyLarge = Base.bodyLarge.copy(fontFamily = Flex600, fontFeatureSettings = Features),
    bodyMedium = Base.bodyMedium.copy(fontFamily = Flex400, fontFeatureSettings = Features),
    bodySmall = Base.bodySmall.copy(fontFamily = Flex400, fontFeatureSettings = Features),
    labelLarge = Base.labelLarge.copy(fontFamily = Flex600, fontFeatureSettings = Features),
    labelMedium = Base.labelMedium.copy(fontFamily = Flex600, fontFeatureSettings = Features),
    labelSmall = Base.labelSmall.copy(fontFamily = Flex600, fontFeatureSettings = Features)
)

val Typography = ShelfTypography
