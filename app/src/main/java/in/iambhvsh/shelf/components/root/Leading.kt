@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.root

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.R
import `in`.iambhvsh.shelf.components.common.expressiveBounce

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
        enter = fadeIn(animationSpec = tween(180)) +
                scaleIn(initialScale = 0.88f, animationSpec = tween(180)) +
                slideInHorizontally(initialOffsetX = { -it / 2 }, animationSpec = tween(180)),
        exit = fadeOut(animationSpec = tween(150)) +
                scaleOut(targetScale = 0.88f, animationSpec = tween(150)) +
                slideOutHorizontally(targetOffsetX = { -it / 2 }, animationSpec = tween(150))
    ) {
        AnimatedContent(
            targetState = leftState,
            transitionSpec = {
                (fadeIn(animationSpec = tween(180)) +
                        scaleIn(initialScale = 0.85f, animationSpec = tween(180)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(150)) +
                                scaleOut(targetScale = 0.85f, animationSpec = tween(150))
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
                        HeaderLeftType.NEW -> {
                            Icon(
                                painter = painterResource(R.drawable.add_icons),
                                contentDescription = description,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        HeaderLeftType.CLOSE -> {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = description,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        HeaderLeftType.BACK -> {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = description,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        HeaderLeftType.NONE -> {}
                    }
                }

                if (state == HeaderLeftType.NEW) {
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
