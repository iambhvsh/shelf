package `in`.iambhvsh.shelf

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import `in`.iambhvsh.shelf.components.collection.CollectionInputSheet
import `in`.iambhvsh.shelf.components.home.BookmarkCard
import `in`.iambhvsh.shelf.components.home.BookmarkListItem
import `in`.iambhvsh.shelf.components.home.EditBookmarkSheet
import `in`.iambhvsh.shelf.components.home.HomeInputSheet
import `in`.iambhvsh.shelf.components.home.NoteEditorSheet
import `in`.iambhvsh.shelf.components.home.TagFilterRow
import `in`.iambhvsh.shelf.components.root.GreetingTitle
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.data.Tag
import `in`.iambhvsh.shelf.features.save.QuickSaveEvents
import `in`.iambhvsh.shelf.features.save.QuickSaveState
import `in`.iambhvsh.shelf.screens.save.QuickSaveScreen
import `in`.iambhvsh.shelf.theme.ShelfTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun headerTitle_respectsEndPaddingWithoutOverlappingRightActions() {
        composeRule.setContent {
            ShelfTheme {
                Box(modifier = Modifier.width(360.dp)) {
                    GreetingTitle(
                        headerTitle = "Very Long Collection Title For Shelf Toolbar",
                        titleXOffset = 80.dp,
                        titleEndPadding = 156.dp,
                        isShiftedToStart = true
                    )
                }
            }
        }
        val titleBounds = composeRule
            .onNodeWithText("Very Long Collection Title For Shelf Toolbar")
            .getUnclippedBoundsInRoot()
        assertTrue(
            "Expected title right edge (${titleBounds.right}) <= 204.dp",
            titleBounds.right <= 204.5.dp
        )
    }

    @Test
    fun bookmarkCard_longPressOnNoteTriggersSelection() {
        var cardLongClicks = 0
        composeRule.setContent {
            ShelfTheme {
                BookmarkCard(
                    imageUrl = null,
                    title = "Card Title",
                    description = "Desc",
                    photoClickUrl = {},
                    bodyClick = {},
                    onLongClick = { cardLongClicks++ },
                    url = "https://kotlinlang.org",
                    note = "Card Note Tap"
                )
            }
        }
        composeRule.onNodeWithText("Card Note Tap").performTouchInput { longClick() }
        composeRule.runOnIdle { assertEquals(1, cardLongClicks) }
    }

    @Test
    fun bookmarkListItem_longPressOnNoteTriggersSelection() {
        var rowLongClicks = 0
        composeRule.setContent {
            ShelfTheme {
                BookmarkListItem(
                    imageUrl = null,
                    title = "Row Title",
                    description = "Desc",
                    photoClickUrl = {},
                    bodyClick = {},
                    onLongClick = { rowLongClicks++ },
                    url = "https://developer.android.com",
                    note = "Row Note Tap"
                )
            }
        }
        composeRule.onNodeWithText("Row Note Tap").performTouchInput { longClick() }
        composeRule.runOnIdle { assertEquals(1, rowLongClicks) }
    }

    @Test
    fun collectionInputSheet_doubleTapSubmitsOnlyOnce() {
        var collectionSaves = 0
        composeRule.setContent {
            ShelfTheme {
                CollectionInputSheet(
                    showBottomSheet = true,
                    onDismissRequest = {},
                    value = "Architecture",
                    onTextChange = {},
                    onSaveClick = { collectionSaves++ }
                )
            }
        }
        composeRule.onNodeWithText("Create Collection").performClick()
        composeRule.onNodeWithText("Create Collection").performClick()
        composeRule.waitForIdle()
        assertEquals(1, collectionSaves)
    }

    @Test
    fun homeInputSheet_doubleTapSubmitsOnlyOnce() {
        var homeSaves = 0
        composeRule.setContent {
            ShelfTheme {
                HomeInputSheet(
                    showBottomSheet = true,
                    onDismissRequest = {},
                    value = "https://kotlinlang.org",
                    onTextChange = {},
                    onSaveClick = { homeSaves++ }
                )
            }
        }
        composeRule.onNodeWithText("Save Bookmark").performClick()
        composeRule.onNodeWithText("Save Bookmark").performClick()
        composeRule.waitForIdle()
        assertEquals(1, homeSaves)
    }

    @Test
    fun noteEditorSheet_resetsTextWhenTargetNoteChanges() {
        var noteInput by mutableStateOf<String?>("Note One")
        composeRule.setContent {
            ShelfTheme {
                NoteEditorSheet(
                    initialNote = noteInput,
                    onSave = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("Note One").assertIsDisplayed()
        composeRule.runOnIdle { noteInput = "Note Two" }
        composeRule.onNodeWithText("Note Two").assertIsDisplayed()
    }

    @Test
    fun editBookmarkSheet_resetsTextWhenTargetBookmarkChanges() {
        var bookmarkInput by mutableStateOf(
            Bookmark(id = 1L, url = "https://one.dev", title = "Title One", description = "Desc One", imageUrl = null)
        )
        composeRule.setContent {
            ShelfTheme {
                EditBookmarkSheet(
                    bookmark = bookmarkInput,
                    onDismissRequest = {},
                    onSaveClick = { _, _ -> }
                )
            }
        }
        composeRule.onAllNodesWithText("Title One").assertCountEquals(2)
        composeRule.runOnIdle {
            bookmarkInput = Bookmark(id = 2L, url = "https://two.dev", title = "Title Two", description = "Desc Two", imageUrl = null)
        }
        composeRule.onAllNodesWithText("Title One").assertCountEquals(0)
        composeRule.onAllNodesWithText("Title Two").assertCountEquals(2)
    }

    @Test
    fun quickSaveScreen_wiringEmitsPickersAndCancel() {
        val events = mutableListOf<QuickSaveEvents>()
        var dismissed = false
        composeRule.setContent {
            ShelfTheme {
                QuickSaveScreen(
                    state = QuickSaveState(
                        bookmarkId = 7L,
                        url = "https://shelf.app",
                        title = "Shelf",
                        selectedCollection = Collection(id = 2L, name = "Reading"),
                        selectedTags = listOf(Tag(id = 3L, name = "Compose"))
                    ),
                    onEvent = { events.add(it) },
                    onDismiss = { dismissed = true }
                )
            }
        }
        composeRule.onNodeWithText("Reading").performClick()
        composeRule.onNodeWithText("Compose").performClick()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.runOnIdle {
            assertTrue(events.contains(QuickSaveEvents.ShowCollectionPicker))
            assertTrue(events.contains(QuickSaveEvents.ShowTagPicker))
            assertTrue(events.contains(QuickSaveEvents.CancelSave))
            assertTrue(dismissed)
        }
    }

    @Test
    fun tagFilterRow_disabledDoesNotFireClicks() {
        var toggledTags = 0
        composeRule.setContent {
            ShelfTheme {
                TagFilterRow(
                    tags = listOf(Tag(id = 1L, name = "HiddenChip")),
                    activeTagIds = emptySet(),
                    onToggleTag = { toggledTags++ },
                    enabled = false
                )
            }
        }
        composeRule.onNodeWithText("HiddenChip").performClick()
        composeRule.runOnIdle { assertEquals(0, toggledTags) }
    }
}
