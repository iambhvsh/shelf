# Changelog

All notable changes to this project will be documented in this file.

## [2.0.6] - 2026-11-25

### Fixed
- **CancellationException Propagation:** Re-throw `CancellationException` in network fetcher, service layer, backup manager, home/collection/setting ops, and updater so coroutine cancellation is never swallowed.
- **BackupManager Dependency Injection:** Replaced inline `LinkMetadataParser()` and `NetworkFetcher()` construction inside `fetchMissingImages` with constructor-injected dependencies to allow testing and avoid leaking instances.
- **QuickSave Blank URL Guard:** `Init` event now short-circuits with an error state when the extracted URL is blank, preventing orphaned bookmark rows.
- **SearchViewModel Error Clearing:** Blank search queries now clear the error state rather than preserving stale error messages.
- **Home/Collection tempBookmark Refresh:** `loadBookmarks` and `selectCollection` now refresh the in-memory `tempBookmark` from the live list after load so sheet-bound state never reads stale data.
- **Collection Delete Fallback:** `deleteCollectionById` falls back to `selectedCollection` when the target collection is not found in the loaded list, preventing silent no-ops.
- **Update Installer Safety:** `installUpdateSuspending` re-throws `CancellationException` before showing the install toast, and closes OkHttp response bodies and `DownloadManager` cursors reliably.

## [2.0.5] - 2026-11-25

### Changed
- **Collection Detail Selection Actions:** Separated deleting bookmarks from removing them from a collection in the selection top bar. The trash action now moves selected bookmarks to Recently Deleted with confirmation (including inside Uncategorised), while a dedicated folder-off button removes bookmarks from the current collection without hiding them.

### Fixed
- **App Lock Initialization & Error Recovery:** Rebuilt biometric prompt configuration on resume when toggling App Lock or PIN fallback in Settings, added an unlock retry screen after biometric errors, and enforced immediate locking on `0ms` timeout round-trips.
- **Database Migration Indices & Large Batch Safety:** Added missing `bookmark_collection_cross_ref` indices in legacy migrations (`1->4`, `2->4`, `3->4`), prioritized active rows in URL lookups, and chunked large multi-select hide, restore, purge, and collection assignment batches for SQLite parameter limits.
- **Reminder Reliability & Timezone Accuracy:** Fixed custom date picker UTC-to-local date conversion across timezones, zeroed sub-minute alarm fields, added an inexact alarm fallback when exact alarm permission is denied, handled app-update alarm rescheduling (`MY_PACKAGE_REPLACED`), preserved navigation state on notification tap, and used `goAsync()` when clearing fired reminders.
- **Quick Save Metadata Persistence & Trash Protection:** Kept background link metadata scraping running after the share sheet closes, prevented early cancel taps from leaving orphaned bookmarks, and re-hid restored trashed links on cancel instead of permanently deleting them.
- **In-App Updater & SAF Backup I/O:** Closed OkHttp response bodies and `DownloadManager` cursors reliably, moved APK staging off the main thread with session cleanup on error, added a notification fallback for Android 14+ background install prompts, enforced HTTPS release links, and moved SAF import/export reads and writes off the main thread with proper stream closing.
- **Backup Restore Fidelity & URL Parsing:** Normalized trailing slashes and scheme casing during JSON and HTML restore, preserved hidden status when restoring trashed backup entries, retained pins/notes/reminders on HTML restore, rejected non-HTTP(S) schemes on HTML import and tab launch, and preserved balanced parentheses and relative `http*` asset paths in URL parsing.
- **Home, Collections & Toolbar State:** Reset Home add-dialog loading state on errors, cleared active tag filters when their tag is deleted, prevented repeated startup metadata scrapes on image-less pages, fixed collection restore reminder rescheduling, hid sort options on the Collections overview tab, exposed the Empty Trash toolbar button, and adapted list bottom padding and tag sheets to system navigation bar and keyboard insets.
- **Navigation Re-entrancy and Touch Isolation:** Guarded forward and back navigation against repeated taps during transitions, gave every route an opaque surface background, and isolated pointer events across transitioning routes, visible top bars, and active search overlays so underlying cards cannot receive clicks.
- **Dynamic Color Toggle After Reset:** Preserved `isDynamicColorSupported` when resetting settings so the Dynamic Color option stays visible in Appearance without requiring an app restart.
- **Imported Backup Thumbnail Recovery:** Added reachable-image verification and bounded background re-fetching after JSON and browser HTML backup imports, alongside single-attempt thumbnail recovery on image load failures in Grid, List, and Search views so expired CDN previews update automatically.
- **Grid Note Chip Editing:** Bound the selected bookmark before opening the note editor from Grid bookmark note chips so edited notes save reliably.
- **Recently Deleted Observer Lifecycle:** Cancelled prior trash flow collectors in `SettingViewModel` before starting a new observation when reopening Recently Deleted.

## [2.0.4] - 2026-11-24

### Added
- **Recently Deleted Screen:** Added a dedicated Recently Deleted section under Data & Storage allowing individual and batch restoration as well as permanent deletion.
- **Reboot Reminder Rescheduling:** Added a boot broadcast receiver to restore active bookmark reminder alarms in AlarmManager across device restarts.
- **Tag Uniqueness Constraint:** Added unique index constraint and deduplicating migration on tags to prevent duplicate tag entries.

### Changed
- **Recently Deleted Interface:** Long-pressing a bookmark activates selection mode with top-bar controls for Select All, Restore, and Delete. Bookmark cards display clean title and description text, support two-way swipe gestures with green restore feedback, and prompt for confirmation before permanently deleting on right swipe.

### Fixed
- **Duplicate Title Rendering:** Resolved an issue where bookmark cards and list rows rendered the title twice when description metadata was blank or absent.
- **List View Pin Indicator:** Added missing star indicator and pin state binding to bookmark list items across Home, Collection Detail, and Search Results.
- **List Image State Lifecycle:** Keyed image failure and aspect ratio remember states to image URLs, preventing recycled list items from suppressing valid image loads.
- **Collection Detail Image Preview & Note Editing:** Enabled full-screen photo preview dialog on thumbnail clicks and note editor opening on note pill clicks in collection detail views.
- **Reminder Lifecycle Synchronization:** Fixed reminder alarm scheduling and cancellation when managing reminders inside collection detail views or deleting individual links from preview sheets, keeping AlarmManager in lockstep with Room entities.
- **Restored Bookmark Reminder Recovery:** Restoring bookmarks from Recently Deleted, collections, or JSON backups now reschedules active future reminders with AlarmManager.
- **Tag Bookmark Query Filtering:** Tag bookmark queries now filter out soft-deleted items, preventing trashed links from appearing under tag filters.
- **Auto-Backup Cross-Reference Invalidation:** Added observers for bookmark-tag and bookmark-collection cross-reference tables in Auto-Backup, ensuring tag assignment and collection changes trigger automatic backups.
- **Browser HTML Import Transaction Atomicity:** Wrapped browser HTML bookmark imports in a database transaction to prevent partial writes during imports.
- **Quick Save Metadata & Collection Pre-population:** Sharing an already-saved link now displays its existing title, description, thumbnail preview, and assigned collection.
- **Delete Confirmation Dialog Messaging:** Deleting bookmarks from preview sheets or the top bar now clarifies that items are moved to Recently Deleted rather than permanently deleted.
- **Backup Data Completeness:** JSON backups and auto-backup now preserve soft-deleted bookmarks and their collection memberships, preventing unrecovered links from being dropped during backup and restore.
- **Cross-Reference Cleanup:** Permanent deletion now purges associated junction rows across collection and tag cross-reference tables.
- **Alarm Lifecycle Management:** Pending reminder alarms are now cancelled when bookmarks are deleted, purged, or when storage is reset.
- **Re-added Bookmark Isolation:** Re-saving a previously deleted URL from the home screen no longer resurfaces historical collection memberships.
- **Share Intent URL Extraction:** Multi-word shared text from apps with preambles now extracts the target URL instead of prepending https to the full string.
- **Collection Preview Query Performance:** Limited collection card preview image concatenation in SQLite to prevent unbounded string allocations on large collections.
- **Quick Save Existing Bookmark Protection:** Cancelling the quick save sheet for an already-saved link now keeps the existing bookmark instead of deleting it.
- **Bookmark Metadata Preservation on Share:** Re-sharing an existing link no longer overwrites custom titles and descriptions with web-scraped metadata.
- **HTML Bookmark Import URL Entity Decoding:** HTML import now unescapes HTML entities in bookmark URLs and handles single-quoted attributes and nested empty folder tags.
- **JSON Import Atomicity & Local Data Retention:** JSON backup imports are executed within a database transaction and skip overwriting local titles, notes, and reminders for existing bookmarks.
- **Auto-Backup Tag Observation:** Backup manager now observes tag changes so creating or modifying tags triggers auto-backup.
- **Legacy Database Migration Path:** Added baseline migrations from database versions 1 through 3 to prevent data loss on upgrades from earlier releases.

## [2.0.3] - 2026-11-23

### Added
- **Automated Verification:** Added test coverage across state pipelines, data mappers, and URL parsers, alongside a GitHub Actions CI pipeline to catch regressions before release.
- **Database Schema Verification:** Enabled Room schema export to validate migrations and prevent schema drift during updates.

### Changed
- **Top Bar Actions:** Consolidated top-right toolbar actions into an overflow menu containing Search and Sort & Filter options, with an Add Bookmark action available when viewing collection details.
- **Automatic Text Ellipsis:** Removed fixed 13-character truncation from collection cards, picker dialogs, quick save sheets, and detail headers in favor of automatic layout-driven ellipsis based on available screen space.

### Fixed
- **Root Screen Insets:** Removed redundant layout wrapper that discarded Scaffold padding and caused potential content clipping.
- **Collection Item Visibility:** Soft-deleted bookmarks are now excluded from collection detail views, aligning the items displayed with the collection card count.
- **Collection Link Creation:** Bookmarks added directly from within a collection are now saved as visible entries instead of soft-deleted items, ensuring they appear in collection counts and on the home screen.
- **Bookmark Card Host Color:** Explicitly bound the bookmark card host label to the surface content color and provided a theme-level default content color, preventing domain names from rendering black in dark theme.
- **Header Title Restoration Animation:** Resolved title clipping and visual freezing when dismissing the search bar by replacing placement-only offsets with layout padding and disabling container bounds clipping during transitions.

## [2.0.2] - 2026-11-22

### Added
- **First-Launch Permission Onboarding:** Added a clean onboarding sheet on initial launch to request essential runtime permissions with clear rationale.
- **Unit Testing Suite:** Added dedicated JSON serialization and Netscape bookmark parser test suites for robust backup verification.
- **Visual Documentation:** Added high-resolution screenshots gallery and updated branding assets in README.

### Changed
- **Bookmark Preview Sheet:** Redesigned bookmark preview sheet with prominent media headers, inline domain favicons, and metadata layout matching bookmark cards.
- **Edit Sheet Media Header:** Embedded the rich preview card inside the bookmark edit sheet for immediate visual feedback.
- **Unified Dialogs:** Aligned data reset confirmation in Settings with delete dialog styling, including bold titles and shape-morphing actions.
- **Tags Strip Refinement:** Harmonized the horizontal tag strip with top toolbar scrolling.

### Fixed
- **Tab Navigation Performance:** Eliminated transition delay and frame drops when switching between Home, Collections, and Settings tabs.

## [2.0.1] - 2026-11-19

### Fixed
- **Splash Screen:** Refined splash screen with high-resolution artwork featuring smooth rounded corners and seamless theme-aware background color synchronization.
- **Settings Data Reset:** Added a safe data reset action with a confirmation dialog in Settings.
- **Top Bar Greetings:** Expanded header greetings with varied, time-aware messages for a warmer and more personal experience.
- **Support Links:** Added Buy Me a Coffee link in About to support ongoing development.

## [2.0.0] - 2026-11-19

### Added
- **Material 3 Expressive:** Upgraded the entire design system to Material 3 Expressive, featuring Google Sans Flex variable typography, tactile spring physics on interactive elements, and expressive organic shapes.
- **Reorganized Settings:** Split the settings screen into dedicated sub-screens (Appearance, General, Security, Data & Storage, and About), neatly organized with segmented card lists.
- **Adaptive Media Previews:** Bookmark cards now intelligently adapt to image aspect ratios in grid view. Vertical media (Reels, Shorts, TikToks, Pins) renders in full portrait, while website previews and OpenGraph banners display horizontally in widescreen.
- **Spring Navigation Transitions:** Navigating between tabs (Home, Collections, Settings) and opening detail screens now features smooth, directional spring slide motions with natural physics.
- **Floating Bottom Toolbar:** Replaced the static bottom bar with a floating pill toolbar that automatically hides when scrolling into collection folders or settings screens.
- **Unified Create Menu:** Added a new '+' action button to the top app bar with a dropdown menu to quickly save a new bookmark or create a collection from either the Home or Collections tab.

### Changed
- **Top App Bar:** Redesigned the header with dynamic greetings and cleaner layout. Long collection titles now cleanly truncate at 13 characters with an ellipsis to prevent overlapping.
- **OLED Dark Mode:** Updated the pure black dark theme with true pitch-black surfaces and refined contrast for OLED screens.
- **Empty States:** Refreshed empty state screens with expressive illustrations, guidance text, and hero typography.

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
- **Seamless Migrations:** We've ensured that transitioning to this version maintains complete data integrity. Your established library (including pins and standalone items) transitions flawlessly to the upgraded schema without losing a byte.

## [1.0.23] - 2026-08-15

### Fixed
- **Compiler Warning:** Removed unnecessary safe call on non-null `ResponseBody` in `NetworkFetcher`.
- **Build Warning:** Replaced deprecated `srcDir()` with `srcDirs()` in build configuration.
- **Changelog Spacing:** Improved spacing below version titles and between list items in the changelog sheet.
- **Settings Order:** Reordered settings sections so Support and Legal now appear above the About group for a more logical flow.

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
