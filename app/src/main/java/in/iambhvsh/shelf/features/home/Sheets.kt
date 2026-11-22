package `in`.iambhvsh.shelf.features.home

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.Clipboard
import `in`.iambhvsh.shelf.components.common.copyToClipboard
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.openChromeTab
import `in`.iambhvsh.shelf.components.home.BookmarkPreviewSheet
import `in`.iambhvsh.shelf.components.home.DeleteConfirmationDialog
import `in`.iambhvsh.shelf.components.home.EditBookmarkSheet
import `in`.iambhvsh.shelf.components.home.NoteEditorSheet
import `in`.iambhvsh.shelf.components.home.PhotoPreview
import `in`.iambhvsh.shelf.components.home.ReminderPickerSheet
import `in`.iambhvsh.shelf.components.home.TagManagerSheet
import `in`.iambhvsh.shelf.components.home.UpdateSheet

@Composable
fun HomeSheets(
    state: HomeState,
    viewModel: HomeViewModel,
    context: Context,
    clipboard: Clipboard,
    bookmarkToDelete: Bookmark?,
    onSetBookmarkToDelete: (Bookmark?) -> Unit
) {
    PhotoPreview(
        isDialog = state.isPhotoPreviewDialog,
        onDismissRequest = { viewModel.homeEvents(HomeEvents.PreviewImageDismissClick) },
        imageURL = state.dialogPhotoUrl
    )

    BookmarkPreviewSheet(
        bookmark = state.tempBookmark,
        showBottomSheet = state.isBodySheet,
        isPinned = state.tempBookmark?.isPinned ?: false,
        onDismissRequest = { viewModel.homeEvents(HomeEvents.BookmarkPreviewDismissClick) },
        openInBrowser = { state.tempBookmark?.url?.let { openChromeTab(url = it, context = context) } },
        copyLinkButtonClick = {
            state.tempBookmark?.url?.let { context.copyToClipboard(it) }
        },
        onShareButtonClick = {
            state.tempBookmark?.url?.let { url ->
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, url)
                    type = "text/plain"
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Link"))
            }
        },
        onPinButtonClick = { state.tempBookmark?.let { viewModel.homeEvents(HomeEvents.TogglePin(it)) } },
        onTagsButtonClick = { viewModel.homeEvents(HomeEvents.ShowTagManager) },
        onNoteButtonClick = { viewModel.homeEvents(HomeEvents.ShowNoteEditor(state.tempBookmark?.note)) },
        onReminderButtonClick = { viewModel.homeEvents(HomeEvents.ShowReminderPicker) },
        onRenameButtonClick = {
            state.tempBookmark?.let {
                viewModel.homeEvents(HomeEvents.ShowEditDialog(it))
            }
        },
        onDeleteButtonClick = {
            state.tempBookmark?.let {
                onSetBookmarkToDelete(it)
            }
        },
        onPhotoClick = { url ->
            viewModel.homeEvents(HomeEvents.PreviewImageClick(url = url))
        }
    )

    if (bookmarkToDelete != null) {
        DeleteConfirmationDialog(
            title = "Delete Bookmark",
            text = "Are you sure you want to permanently delete this bookmark?",
            onConfirm = {
                viewModel.homeEvents(HomeEvents.DeleteBookmark(bookmarkToDelete))
                onSetBookmarkToDelete(null)
            },
            onDismiss = {
                onSetBookmarkToDelete(null)
            }
        )
    }

    val bookmarkToEdit = state.tempBookmark
    if (state.showRenameDialog && bookmarkToEdit != null) {
        EditBookmarkSheet(
            bookmark = bookmarkToEdit,
            onDismissRequest = { viewModel.homeEvents(HomeEvents.HideEditDialog) },
            onSaveClick = { newTitle, newDescription ->
                viewModel.homeEvents(HomeEvents.UpdateBookmarkDetails(bookmarkToEdit.id, newTitle, newDescription))
            }
        )
    }

    if (state.showTagManager) {
        TagManagerSheet(
            tags = state.tags,
            selectedTags = state.tempBookmarkTags,
            onToggleTag = { tag, isChecked ->
                viewModel.homeEvents(HomeEvents.ToggleTagForBookmark(tag, isChecked))
            },
            onCreateTag = { name ->
                viewModel.homeEvents(HomeEvents.CreateTag(name))
            },
            onDeleteTag = { tag ->
                viewModel.homeEvents(HomeEvents.DeleteTag(tag.id))
            },
            onDismiss = { viewModel.homeEvents(HomeEvents.HideTagManager) }
        )
    }

    if (state.showNoteEditor) {
        NoteEditorSheet(
            initialNote = state.noteEditorText,
            onSave = { note ->
                state.tempBookmark?.let {
                    viewModel.homeEvents(HomeEvents.UpdateNote(it.id, note))
                }
            },
            onDismiss = { viewModel.homeEvents(HomeEvents.HideNoteEditor) }
        )
    }

    if (state.showReminderPicker) {
        ReminderPickerSheet(
            onSetReminder = { timeInMillis ->
                state.tempBookmark?.let {
                    viewModel.homeEvents(HomeEvents.SetReminder(it.id, timeInMillis))
                }
            },
            onCancelReminder = {
                state.tempBookmark?.let {
                    viewModel.homeEvents(HomeEvents.CancelReminder(it.id))
                }
            },
            hasExistingReminder = state.tempBookmark?.reminderTime != null,
            onDismiss = { viewModel.homeEvents(HomeEvents.HideReminderPicker) }
        )
    }

    if (state.showUpdateSheet) {
        UpdateSheet(
            onDismiss = { viewModel.homeEvents(HomeEvents.DismissUpdateSheet) },
            onInstall = { viewModel.homeEvents(HomeEvents.InstallUpdate) }
        )
    }
}
