@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.root

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.R

enum class HeaderLeftType {
    NEW,
    BACK,
    CLOSE,
    NONE
}

@Composable
fun LeftActionSlot(
    leftState: HeaderLeftType,
    isSearching: Boolean,
    onBack: () -> Unit,
    onCloseSearch: () -> Unit,
    onCloseSelection: () -> Unit,
    onNewBookmarkClick: () -> Unit,
    onNewCollectionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewMenu by remember { mutableStateOf(false) }

    LaunchedEffect(leftState) {
        showNewMenu = false
    }

    AnimatedVisibility(
        visible = leftState != HeaderLeftType.NONE,
        modifier = modifier,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                scaleIn(initialScale = 0.92f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) +
                slideInHorizontally(initialOffsetX = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessLow)),
        exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                slideOutHorizontally(targetOffsetX = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessLow))
    ) {
        AnimatedContent(
            targetState = leftState,
            transitionSpec = {
                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                        scaleIn(initialScale = 0.92f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)))
                    .togetherWith(
                        fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                                scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessLow))
                    )
            },
            label = "LeftButtonMorph"
        ) { state ->
            val action: () -> Unit = when (state) {
                HeaderLeftType.NEW -> { { showNewMenu = !showNewMenu } }
                HeaderLeftType.CLOSE -> if (isSearching) onCloseSearch else onCloseSelection
                HeaderLeftType.BACK -> onBack
                HeaderLeftType.NONE -> ({})
            }

            val description = when (state) {
                HeaderLeftType.NEW -> "New"
                HeaderLeftType.CLOSE -> if (isSearching) "Close search" else "Close selection"
                HeaderLeftType.BACK -> "Back"
                HeaderLeftType.NONE -> ""
            }

            Box(modifier = Modifier.padding(start = 16.dp)) {
                if (state == HeaderLeftType.BACK) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable(onClick = action),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = description,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    TopBarButton(
                        onClick = action,
                        contentDescription = description,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        when (state) {
                            HeaderLeftType.NEW -> {
                                Icon(
                                    painter = painterResource(R.drawable.add_icons),
                                    contentDescription = description,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            HeaderLeftType.CLOSE -> {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = description,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            HeaderLeftType.BACK -> {}
                            HeaderLeftType.NONE -> {}
                        }
                    }
                }

                if (state == HeaderLeftType.NEW) {
                    NewActionDropdownMenu(
                        expanded = showNewMenu,
                        onDismissRequest = { showNewMenu = false },
                        onNewBookmarkClick = onNewBookmarkClick,
                        onNewCollectionClick = onNewCollectionClick
                    )
                }
            }
        }
    }
}
