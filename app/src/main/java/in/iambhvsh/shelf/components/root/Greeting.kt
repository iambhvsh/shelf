@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.root

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import `in`.iambhvsh.shelf.theme.HeroFont
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun GreetingTitle(
    headerTitle: String,
    titleXOffset: Dp,
    isShiftedToStart: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = headerTitle,
        transitionSpec = {
            (fadeIn(animationSpec = tween(180)) +
                    scaleIn(initialScale = 0.94f, animationSpec = tween(180)))
                .togetherWith(
                    fadeOut(animationSpec = tween(150)) +
                            scaleOut(targetScale = 0.94f, animationSpec = tween(150))
                )
        },
        label = "HeaderTitleAnimation",
        modifier = modifier.offset(x = titleXOffset)
    ) { target ->
        if (target == "home_header") {
            val greeting = remember { randomGreeting() }
            var stage by remember { mutableIntStateOf(0) }
            LaunchedEffect(Unit) {
                delay(1600)
                stage = 1
            }

            AnimatedContent(
                targetState = stage,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 2 })
                        .togetherWith(fadeOut() + slideOutVertically { -it / 2 })
                },
                label = "HomeTitleAnimation"
            ) { currentStage ->
                Text(
                    text = if (currentStage == 0) greeting else "Shelf",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = HeroFont),
                    fontWeight = if (currentStage == 1) FontWeight.ExtraBold else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                text = target,
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = HeroFont),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = if (isShiftedToStart) TextAlign.Start else TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun randomGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val timeGreeting = when (hour) {
        in 5..11 -> listOf(
            "Morning",
            "Rise up",
            "Early bird",
            "Fresh day",
            "Sunrise",
            "New day",
            "Daybreak",
            "Good day",
            "First cup"
        )
        in 12..16 -> listOf(
            "Afternoon",
            "Midday",
            "Pause",
            "Break time",
            "Relax",
            "Breathe",
            "Quick look",
            "Slow down",
            "Catch up"
        )
        in 17..21 -> listOf(
            "Evening",
            "Sunset",
            "Wind down",
            "Unwind",
            "Dusk",
            "Sundown",
            "Chill out",
            "Nightfall",
            "Rest easy"
        )
        else -> listOf(
            "Night owl",
            "Midnight",
            "Late hours",
            "Deep night",
            "Starlight",
            "Quiet time",
            "Moonlit",
            "Night read",
            "Stillness"
        )
    }
    val common = listOf(
        "Hey there",
        "Your Shelf",
        "Welcome",
        "Explore",
        "Read later",
        "Bookmarks",
        "Discover",
        "Saved",
        "Hello",
        "Hi there",
        "Curated",
        "Keepers",
        "Treasures",
        "Stashed",
        "Finds",
        "Curious",
        "Wander",
        "Collected",
        "Back again",
        "Favorites",
        "Read on",
        "Check-in"
    )
    return (timeGreeting + common).random()
}
