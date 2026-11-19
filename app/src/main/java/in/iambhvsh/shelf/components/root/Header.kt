@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.root

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

private class SmoothedHeaderValues(
    val alpha: State<Float>,
    val offset: State<Float>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberSmoothedHeaderValues(state: TopAppBarState): SmoothedHeaderValues {
    val alpha = animateFloatAsState(
        targetValue = (1f - state.collapsedFraction).coerceIn(0f, 1f),
        animationSpec = tween(180),
        label = "SmoothedHeaderAlpha"
    )
    val offset = animateFloatAsState(
        targetValue = state.heightOffset,
        animationSpec = tween(180),
        label = "SmoothedHeaderOffset"
    )
    return SmoothedHeaderValues(alpha, offset)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfHeader(
    currentTab: Int,
    isDeepScreen: Boolean,
    deepScreenTitle: String? = null,
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    isAllSelected: Boolean = false,
    isSearching: Boolean = false,
    searchQuery: String = "",
    searchPlaceholder: String = "Search bookmarks\u2026",
    onSearchQueryChange: (String) -> Unit = {},
    focusRequester: FocusRequester? = null,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit = {},
    onNewBookmarkClick: () -> Unit = {},
    onNewCollectionClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSortClick: () -> Unit = {},
    onCloseSelection: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onDeselectAll: () -> Unit = {},
    onDeleteSelected: (() -> Unit)? = null,
    onAddToCollection: (() -> Unit)? = null,
    onRenameSelected: (() -> Unit)? = null,
    onCloseSearch: () -> Unit = {}
) {
    val smoothed = rememberSmoothedHeaderValues(scrollBehavior.state)
    val smoothedAlpha = smoothed.alpha
    val smoothedOffset = smoothed.offset

    val sideSlotWidth = 72.dp
    val isShiftedToStart = isDeepScreen || isSelectionMode

    val titleBias by animateFloatAsState(
        targetValue = if (isShiftedToStart) -1f else 0f,
        animationSpec = tween(180),
        label = "TitleAlignment"
    )

    val titleXOffset by animateDpAsState(
        targetValue = if (isShiftedToStart) 68.dp else 0.dp,
        animationSpec = tween(180),
        label = "TitleOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = smoothedAlpha.value }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        Color.Transparent
                    )
                )
            )
    ) {
        CenterAlignedTopAppBar(
            scrollBehavior = scrollBehavior,
            windowInsets = WindowInsets.statusBars,
            navigationIcon = {},
            title = {},
            actions = {},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
        )

        // Center / Dynamic Title Area
        Box(
            modifier = Modifier
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
            if (isSearching) {
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

        // Left Navigation Slot (New / Back / Close button)
        val leftState = when {
            isSearching -> HeaderLeftType.CLOSE
            isSelectionMode -> HeaderLeftType.CLOSE
            isDeepScreen -> HeaderLeftType.BACK
            currentTab == 0 || currentTab == 1 -> HeaderLeftType.NEW
            else -> HeaderLeftType.NONE
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset { IntOffset(0, smoothedOffset.value.roundToInt()) }
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(64.dp)
                .widthIn(min = sideSlotWidth)
                .graphicsLayer {
                    val a = smoothedAlpha.value
                    alpha = a
                    val scale = 1f - ((1f - a) * 0.12f)
                    scaleX = scale
                    scaleY = scale
                }
                .zIndex(2f),
            contentAlignment = Alignment.CenterStart
        ) {
            LeftActionSlot(
                leftState = leftState,
                isSearching = isSearching,
                onBack = onBack,
                onCloseSearch = onCloseSearch,
                onCloseSelection = onCloseSelection,
                onNewBookmarkClick = onNewBookmarkClick,
                onNewCollectionClick = onNewCollectionClick
            )
        }

        // Right Actions Slot
        if (!isSearching) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(0, smoothedOffset.value.roundToInt()) }
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(64.dp)
                    .widthIn(min = sideSlotWidth)
                .graphicsLayer {
                    val a = smoothedAlpha.value
                    alpha = a
                    val scale = 1f - ((1f - a) * 0.12f)
                    scaleX = scale
                    scaleY = scale
                }
                .zIndex(3f),
            contentAlignment = Alignment.CenterEnd
        ) {
            RightActionSlot(
                isSelectionMode = isSelectionMode,
                currentTab = currentTab,
                isAllSelected = isAllSelected,
                onSelectAll = onSelectAll,
                onDeselectAll = onDeselectAll,
                onSearchClick = onSearchClick,
                onSortClick = onSortClick,
                onAddToCollection = onAddToCollection,
                onRenameSelected = onRenameSelected,
                onDeleteSelected = onDeleteSelected
            )
        }
        }
    }
}
