@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.root

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

class SmoothedHeaderValues(
    val alpha: State<Float>,
    val offset: State<Float>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSmoothedHeaderValues(state: TopAppBarState): SmoothedHeaderValues {
    val alpha = animateFloatAsState(
        targetValue = (1f - state.collapsedFraction).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "SmoothedHeaderAlpha"
    )
    val offset = animateFloatAsState(
        targetValue = state.heightOffset,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "SmoothedHeaderOffset"
    )
    return SmoothedHeaderValues(alpha, offset)
}

fun headerTitleTransitionSpec(targetIsSearching: Boolean): ContentTransform {
    return if (targetIsSearching) {
        (slideInHorizontally(
            initialOffsetX = { it / 3 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )).togetherWith(
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        )
    } else {
        (slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )).togetherWith(
            slideOutHorizontally(
                targetOffsetX = { it / 3 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        )
    }
}

@Composable
fun HeaderCenterSlot(
    isSearching: Boolean,
    titleBias: Float,
    titleXOffset: Dp,
    isShiftedToStart: Boolean,
    isSelectionMode: Boolean,
    selectedCount: Int,
    isDeepScreen: Boolean,
    deepScreenTitle: String?,
    currentTab: Int,
    searchQuery: String,
    searchPlaceholder: String,
    onSearchQueryChange: (String) -> Unit,
    focusRequester: FocusRequester?,
    smoothedAlpha: State<Float>,
    smoothedOffset: State<Float>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(64.dp)
            .offset { IntOffset(0, smoothedOffset.value.roundToInt()) }
            .graphicsLayer {
                val a = smoothedAlpha.value
                alpha = a
                val scale = 1f - ((1f - a) * 0.08f)
                scaleX = scale
                scaleY = scale
            }
            .zIndex(1f),
        contentAlignment = BiasAlignment(horizontalBias = titleBias, verticalBias = 0f)
    ) {
        AnimatedContent(
            targetState = isSearching,
            transitionSpec = { headerTitleTransitionSpec(targetState) },
            label = "HeaderTitleSearchTransition"
        ) { searching ->
            if (searching) {
                SearchField(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    searchPlaceholder = searchPlaceholder,
                    focusRequester = focusRequester
                )
            } else {
                val headerTitle = when {
                    isSelectionMode -> "Selected $selectedCount"
                    isDeepScreen -> deepScreenTitle ?: "Detail"
                    currentTab == 0 -> "home_header"
                    currentTab == 1 -> "Collections"
                    else -> "Settings"
                }
                GreetingTitle(
                    headerTitle = headerTitle,
                    titleXOffset = titleXOffset,
                    isShiftedToStart = isShiftedToStart
                )
            }
        }
    }
}
