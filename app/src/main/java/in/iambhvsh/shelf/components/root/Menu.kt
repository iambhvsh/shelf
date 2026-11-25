package `in`.iambhvsh.shelf.components.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.R

@Composable
fun NewActionDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onNewBookmarkClick: () -> Unit,
    onNewCollectionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            offset = DpOffset(x = 0.dp, y = 10.dp),
            modifier = modifier
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
                    onDismissRequest()
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
                    onDismissRequest()
                    onNewCollectionClick()
                }
            )
        }
    }
}

@Composable
fun MoreActionDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSearchClick: () -> Unit,
    onSortClick: (() -> Unit)? = null,
    onAddClick: (() -> Unit)? = null,
    addLabel: String = "Add Bookmark",
    modifier: Modifier = Modifier
) {
    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            offset = DpOffset(x = 0.dp, y = 10.dp),
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(vertical = 4.dp)
        ) {
            if (onAddClick != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = addLabel,
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
                        onDismissRequest()
                        onAddClick()
                    }
                )
            }
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.search_icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = {
                    onDismissRequest()
                    onSearchClick()
                }
            )
            if (onSortClick != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Sort & Filter",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.filter_icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    onClick = {
                        onDismissRequest()
                        onSortClick()
                    }
                )
            }
        }
    }
}
