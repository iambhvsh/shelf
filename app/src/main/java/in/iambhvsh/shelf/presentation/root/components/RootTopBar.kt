@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.presentation.root.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import `in`.iambhvsh.shelf.R
import `in`.iambhvsh.shelf.ui.components.expressiveBounce
import `in`.iambhvsh.shelf.ui.theme.HeroFont
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private class SmoothedHeaderValues(
    val alpha: State<Float>,
    val offset: State<Float>
)

private enum class LeftButtonType {
    NEW,
    BACK,
    CLOSE,
    NONE
}

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
    val focusManager = LocalFocusManager.current

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
                // Search Pill Field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 68.dp, end = 16.dp)
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = searchPlaceholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable { onSearchQueryChange("") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                val headerTitle = when {
                    isSelectionMode -> "Selected $selectedCount"
                    isDeepScreen -> deepScreenTitle ?: "Detail"
                    currentTab == 0 -> "home_header"
                    currentTab == 1 -> "Collections"
                    else -> "Settings"
                }

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
                    modifier = Modifier.offset(x = titleXOffset)
                ) { target ->
                    if (target == "home_header") {
                        val greeting = remember {
                            listOf(
                                "Welcome",
                                "Hey there",
                                "Your Shelf",
                                "Read Later",
                                "Explore"
                            ).random()
                        }
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
        }

        // Left Navigation Slot (New / Back / Close button)
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
            var showNewMenu by remember { mutableStateOf(false) }

            LaunchedEffect(currentTab, isDeepScreen, isSearching, isSelectionMode) {
                showNewMenu = false
            }

            val leftState = when {
                isSearching -> LeftButtonType.CLOSE
                isSelectionMode -> LeftButtonType.CLOSE
                isDeepScreen -> LeftButtonType.BACK
                currentTab == 0 || currentTab == 1 -> LeftButtonType.NEW
                else -> LeftButtonType.NONE
            }

            AnimatedVisibility(
                visible = leftState != LeftButtonType.NONE,
                enter = fadeIn(animationSpec = tween(180)) +
                        scaleIn(
                            initialScale = 0.88f,
                            animationSpec = tween(180)
                        ) +
                        slideInHorizontally(initialOffsetX = { -it / 2 }, animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(150)) +
                        scaleOut(targetScale = 0.88f, animationSpec = tween(150)) +
                        slideOutHorizontally(targetOffsetX = { -it / 2 }, animationSpec = tween(150))
            ) {
                AnimatedContent(
                    targetState = leftState,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(180)) +
                                scaleIn(
                                    initialScale = 0.85f,
                                    animationSpec = tween(180)
                                ))
                            .togetherWith(
                                fadeOut(animationSpec = tween(150)) +
                                        scaleOut(targetScale = 0.85f, animationSpec = tween(150))
                            )
                    },
                    label = "LeftButtonMorph"
                ) { state ->
                    val action: () -> Unit = when (state) {
                        LeftButtonType.NEW -> { { showNewMenu = !showNewMenu } }
                        LeftButtonType.CLOSE -> if (isSearching) onCloseSearch else onCloseSelection
                        LeftButtonType.BACK -> onBack
                        LeftButtonType.NONE -> ({})
                    }
                    val description = when (state) {
                        LeftButtonType.NEW -> "New"
                        LeftButtonType.CLOSE -> if (isSearching) "Close search" else "Close selection"
                        LeftButtonType.BACK -> "Back"
                        LeftButtonType.NONE -> ""
                    }

                    Box(
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                .expressiveBounce(scaleDown = 0.92f)
                                .clickable(onClick = action),
                            contentAlignment = Alignment.Center
                        ) {
                            when (state) {
                                LeftButtonType.NEW -> {
                                    Icon(
                                        painter = painterResource(R.drawable.add_icons),
                                        contentDescription = description,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                LeftButtonType.CLOSE -> {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = description,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                LeftButtonType.BACK -> {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = description,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                LeftButtonType.NONE -> {}
                            }
                        }

                        if (state == LeftButtonType.NEW) {
                            MaterialTheme(
                                shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
                            ) {
                                DropdownMenu(
                                    expanded = showNewMenu,
                                    onDismissRequest = { showNewMenu = false },
                                    offset = DpOffset(x = 0.dp, y = 10.dp),
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .padding(vertical = 4.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "New Bookmark",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(R.drawable.bookmark_add),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        onClick = {
                                            showNewMenu = false
                                            onNewBookmarkClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "New Collection",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.CreateNewFolder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        onClick = {
                                            showNewMenu = false
                                            onNewCollectionClick()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Right Actions Slot
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
            if (!isSearching) {
                AnimatedContent(
                    targetState = if (isSelectionMode) "selection" else "normal",
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(180)) +
                                scaleIn(
                                    initialScale = 0.92f,
                                    animationSpec = tween(180)
                                ) +
                                slideInHorizontally(initialOffsetX = { it / 2 }, animationSpec = tween(180)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(150)) +
                                        scaleOut(targetScale = 0.92f, animationSpec = tween(150)) +
                                        slideOutHorizontally(targetOffsetX = { it / 2 }, animationSpec = tween(150))
                            )
                    },
                    label = "RightActionsContent"
                ) { state ->
                    if (state == "selection") {
                        Row(
                            modifier = Modifier.padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isAllSelected) {
                                IconButton(
                                    onClick = onDeselectAll,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .expressiveBounce(scaleDown = 0.92f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.deselct_all),
                                        contentDescription = "Deselect all",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = onSelectAll,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .expressiveBounce(scaleDown = 0.92f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.selectall_icon),
                                        contentDescription = "Select all",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            if (onAddToCollection != null) {
                                IconButton(
                                    onClick = onAddToCollection,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .expressiveBounce(scaleDown = 0.92f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.bookmark_add),
                                        contentDescription = "Add to collection",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            if (onRenameSelected != null) {
                                IconButton(
                                    onClick = onRenameSelected,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .expressiveBounce(scaleDown = 0.92f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Rename",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            if (onDeleteSelected != null) {
                                IconButton(
                                    onClick = onDeleteSelected,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .expressiveBounce(scaleDown = 0.92f)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.delete_icon),
                                        contentDescription = "Delete selected",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Normal mode: on Home and Collections tabs (and collection detail), show Search + Sort!
                        val showRightActions = currentTab == 0 || currentTab == 1

                        AnimatedVisibility(
                            visible = showRightActions,
                            enter = fadeIn(animationSpec = tween(180)) +
                                    scaleIn(initialScale = 0.88f, animationSpec = tween(180)),
                            exit = fadeOut(animationSpec = tween(150)) +
                                    scaleOut(targetScale = 0.88f, animationSpec = tween(150))
                        ) {
                            Row(
                                modifier = Modifier.padding(end = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                        .expressiveBounce(scaleDown = 0.92f)
                                        .clickable(onClick = onSearchClick),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.search_icon),
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                        .expressiveBounce(scaleDown = 0.92f)
                                        .clickable(onClick = onSortClick),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.filter_icon),
                                        contentDescription = "Sort & Filter",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
