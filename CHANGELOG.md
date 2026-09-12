# Changelog

All notable changes to this project will be documented in this file.

## [1.0.25] - 2026-09-12

### Added
- **Quick Save:** Sharing links to Shelf now opens a compact, non-disruptive bottom sheet to quickly stash your links without leaving your current app.
- **Uncategorised Collection:** Easily find all your loose links! Added a virtual Uncategorised collection that automatically gathers all bookmarks not assigned to any folder.
- **Custom Backup Location:** You now have full control over where your backups are stored. Added the ability to choose a custom local folder for all your backups.
- **App Lock Timeout:** Added a customizable timeout setting for the app lock. Quickly switch between apps without having to unlock Shelf every time!

### Fixed
- **Code Cleanliness:** Cleaned up internal logic and polished the codebase for better maintainability.

## [1.0.24] - 2026-08-22

### Added
- **Comprehensive Backups (v2):** Overhauled the auto-backup system! Tags, notes, reminder times, pins, and hidden states are now fully exported and restored flawlessly. Rest easy knowing your exact library state is backed up.
- **Deletion Confirmation:** Added a safety net! When deleting multiple bookmarks or collections from the selection mode, a confirmation dialog now pops up preventing accidental permanent deletion.
- **UI Refresh:** Reorganized the bookmark action sheet with a more logical order and clearer titles.
- **Scoped Bookmarking:** You can now bypass the main feed entirely by adding URLs directly from within a folder. Simply hit the floating action button while viewing any collection to stash links exclusively there.
- **Smart Validation:** The app now proactively checks for identical entries as soon as you attempt to save a link inside a folder, providing immediate feedback rather than delaying the warning.
- **Input Sanitization:** Submitting blank entries through the collection add dialog no longer triggers any background actions, quietly dismissing the prompt instead.
- **Performance Boosts:** Navigating into folders and loading your curated lists feels significantly faster, thanks to new structural optimizations applied to the underlying database.

### Fixed
- **Smart Reminders:** Fixed a bug where tapping a reminder notification would display a blank preview sheet. The preview now correctly fetches the exact bookmark directly from the database, ensuring it opens flawlessly even if it's hidden inside a specific collection!
- **Seamless Migrations:** We've ensured that transitioning to this version maintains complete data integrity. Your established library—including pins and standalone items—transitions flawlessly to the upgraded schema without losing a byte.

## [1.0.23] - 2026-08-15

### Fixed
- **Compiler Warning:** Removed unnecessary safe call on non-null `ResponseBody` in `NetworkFetcher`.
- **Build Warning:** Replaced deprecated `srcDir()` with `srcDirs()` in build configuration.
- **Changelog Spacing:** Improved spacing below version titles and between list items in the changelog sheet.
- **Settings Order:** Reordered settings sections — Support and Legal now appear above the About group for a more logical flow.

### Added
- **Orion Store:** Shelf is now listed on the [Orion Store](https://rookieenough.github.io/Orion-Data/redirect.html?id=shelf).

## [1.0.22] - 2026-08-15


### Fixed
- **Bookmark Renaming:** Fixed an issue where renaming a bookmark from the preview sheet would not save the new title due to state being cleared too early.
- **Top Bar Titles:** The top bar title now correctly resets to "Collections" when returning from a collection folder.
- **Smooth Navigation:** Navigation between tabs and opening/closing collections now features a much smoother cross-fade transition.
- **Changelog UI:** Improved the changelog design to be visually appealing, full-width, and appropriately spaced.
- **Settings Clarity:** Revamped the Settings icons with carefully selected unique outlines, avoiding repetition and grouping related options nicely.
- **Backup Imports (Instagram links):** Fixed a bug where importing older backups would display placeholders for Instagram reels; expired CDN links are now purged on import, allowing the app to automatically fetch fresh thumbnails upon startup!

## [1.0.21] - 2026-08-15

### Fixed
- **Dialog Data Loss:** Resolved a critical bug where opening any dialog (like Rename, Tags, Notes, Reminders) from the bookmark preview sheet would immediately "forget" which bookmark was selected, preventing changes from saving.
- **Collection Pinning Sorting:** Pinned bookmarks inside a collection now properly float to the very top, matching the behavior on the Home tab.
- **Collection Titles:** When viewing a collection, the top app bar now beautifully displays the collection's name (with an ellipsis for long names) instead of a generic title.

## [1.0.20] - 2026-08-15

### Added
- **Rename Support:** You can now rename links and collections! For links, tap the bookmark to access the new "Rename" option in the preview sheet. For collections, long press a collection to enter selection mode, then tap the edit icon in the top bar.
- **Collection Features Parity:** The bookmark preview sheet inside collections is now completely on par with the home screen. You can add tags, notes, reminders, pin, and rename directly from within the collection view.

### Fixed
- **Disappearing Bookmarks:** Fixed a bug where returning from the collection details screen back to the home screen, and then returning again, would cause the collection list to disappear. 
- **Settings Streamlined:** Simplified the settings screen by consolidating the Community and Legal sections under "About Shelf" for a cleaner, unified look.
- **Under-the-hood:** Addressed a minor linting issue regarding system broadcast receivers in the updater on Android 14.

## [1.0.19] - 2026-08-15

### Added
- **In-App Changelog Viewer:** You can now see what's new directly from the app! Find the new "Changelog" option under Settings > About.
- **Native Splash Screen:** Implemented the native Android 12+ Splash Screen API. The app now launches with a seamless, theme-aware background instead of a harsh white flash.

## [1.0.18] - 2026-08-08

### Fixed
- **GitHub Actions:** Fixed the release workflow to properly extract and display changelog text on the release card instead of just a link.

## [1.0.17] - 2026-08-08

### Added
- **Native Share Out:** Added a native Android "Share Link" option in the bookmark preview bottom sheet. You can now instantly share saved links to apps like WhatsApp, Twitter, or Messages.
- **Documentation:** The README now accurately highlights the app's hidden gems, including the seamless in-app updater and intelligent metadata crawling.
