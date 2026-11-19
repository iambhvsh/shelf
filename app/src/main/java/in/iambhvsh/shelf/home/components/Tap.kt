package `in`.iambhvsh.shelf.home.components

import android.content.Context
import androidx.compose.ui.platform.Clipboard
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.openChromeTab
import `in`.iambhvsh.shelf.home.HomeEvents
import `in`.iambhvsh.shelf.home.HomeViewModel
import `in`.iambhvsh.shelf.setting.TapAction

fun handleTap(
    item: Bookmark,
    tapAction: TapAction,
    context: Context,
    clipboard: Clipboard,
    viewModel: HomeViewModel
) {
    when (tapAction) {
        TapAction.OPEN_BROWSER -> openChromeTab(item.url, context)
        TapAction.COPY_LINK -> item.url.let { clipboard.nativeClipboard.setPrimaryClip(android.content.ClipData.newPlainText("", it)) }
        TapAction.SHOW_PREVIEW -> viewModel.homeEvents(HomeEvents.BookmarkPreviewClick(item))
    }
}
