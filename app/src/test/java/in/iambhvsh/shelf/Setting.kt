package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.BookmarkRepositoryImpl
import `in`.iambhvsh.shelf.features.setting.SettingEvents
import `in`.iambhvsh.shelf.features.setting.SettingState
import `in`.iambhvsh.shelf.features.setting.SettingViewModel
import `in`.iambhvsh.shelf.features.setting.TapAction
import `in`.iambhvsh.shelf.features.setting.ViewMode
import `in`.iambhvsh.shelf.theme.AccentColor
import `in`.iambhvsh.shelf.theme.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingStateTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @After
    fun tearDown() {
        SessionManager.reset()
    }

    @Test
    fun testDefaultSettingState_hasStandardDefaults() {
        val state = SettingState()
        assertEquals(ThemeMode.SYSTEM, state.themeMode)
        assertEquals(TapAction.SHOW_PREVIEW, state.tapAction)
        assertEquals(ViewMode.GRID, state.viewMode)
        assertTrue(state.dynamicColor)
        assertEquals(AccentColor.PERIWINKLE, state.accentColor)
        assertFalse(state.appLockEnabled)
        assertEquals(0L, state.appLockTimeout)
    }

    @Test
    fun testThemeModeTransitions() {
        var state = SettingState()
        state = state.copy(themeMode = ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, state.themeMode)

        state = state.copy(themeMode = ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, state.themeMode)

        state = state.copy(themeMode = ThemeMode.OLED)
        assertEquals(ThemeMode.OLED, state.themeMode)
    }

    @Test
    fun testTapActionTransitions() {
        var state = SettingState()
        state = state.copy(tapAction = TapAction.OPEN_BROWSER)
        assertEquals(TapAction.OPEN_BROWSER, state.tapAction)

        state = state.copy(tapAction = TapAction.COPY_LINK)
        assertEquals(TapAction.COPY_LINK, state.tapAction)
    }

    @Test
    fun testViewModeToggle() {
        var state = SettingState(viewMode = ViewMode.GRID)
        state = state.copy(viewMode = ViewMode.LIST)
        assertEquals(ViewMode.LIST, state.viewMode)
    }

    @Test
    fun testAppLockTimeoutConfigurations() {
        var state = SettingState()
        state = state.copy(appLockEnabled = true, appLockTimeout = 15000L)
        assertTrue(state.appLockEnabled)
        assertEquals(15000L, state.appLockTimeout)

        state = state.copy(appLockTimeout = 60000L)
        assertEquals(60000L, state.appLockTimeout)
    }

    @Test
    fun testAccentColorsHaveDefinedLabelsAndValues() {
        assertEquals(9, AccentColor.values().size)
        for (accent in AccentColor.values()) {
            assertNotNull(accent.label)
            assertTrue(accent.label.isNotBlank())
            assertNotNull(accent.seed)
        }
    }

    @Test
    fun testResetPreservesDynamicColorCapability() {
        val current = SettingState(
            dynamicColor = false,
            isDynamicColorSupported = true
        )
        val resetState = SettingState(
            dynamicColor = true,
            isDynamicColorSupported = current.isDynamicColorSupported,
            resetSuccessToast = true
        )
        assertTrue(resetState.dynamicColor)
        assertTrue(resetState.isDynamicColorSupported)
        assertTrue(resetState.resetSuccessToast)
    }

    @Test
    fun testSettingViewModel_appLockTogglesAndResetEverythingClearsAllStateAndReminders() = runTest {
        val db = FakeBookmarkDatabase()
        val settings = FakeSettingsRepository().apply {
            themeMode = ThemeMode.OLED
            appLockEnabled = false
        }
        val backup = FakeBackupManager(db, db.store, settings)
        val updates = FakeUpdateManager(hasUpdateResult = true)
        val reminders = FakeReminderManager()
        val repo = BookmarkRepositoryImpl(db.store.bookmarkDao, db.store.collectionDao, db.store.tagDao)

        val id1 = db.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://1.dev", title = "1", description = null, imageUrl = null, reminderTime = 5000L)
        )
        val id2 = db.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://2.dev", title = "2", description = null, imageUrl = null, isHidden = true)
        )

        val vm = SettingViewModel(
            settingsRepository = settings,
            backupManager = backup,
            updateManager = updates,
            changelogLoader = FakeChangelogLoader(),
            database = db,
            bookmarkRepository = repo,
            reminderManager = reminders,
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, vm.state.value.trashedCount)

        SessionManager.isUnlocked = false
        vm.onEvent(SettingEvents.ToggleAppLock(true))
        assertTrue(settings.appLockEnabled)
        assertTrue(vm.state.value.appLockEnabled)
        assertTrue(SessionManager.isUnlocked)

        vm.onEvent(SettingEvents.ToggleAppLockUsePin(true))
        assertTrue(settings.appLockUsePinEnabled)

        vm.onEvent(SettingEvents.ConfirmReset)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, db.clearedTablesCount)
        assertTrue(backup.autoBackupStopped)
        assertTrue(reminders.cancelled.containsAll(listOf(id1, id2)))
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertFalse(settings.appLockEnabled)
        assertTrue(vm.state.value.resetSuccessToast)

        vm.onEvent(SettingEvents.ResetSuccessToastShown)
        assertFalse(vm.state.value.resetSuccessToast)
    }

    @Test
    fun testSettingViewModel_trashLifecycleRestoreReschedulesAndEmptyTrashCancelsReminders() = runTest {
        val db = FakeBookmarkDatabase()
        val settings = FakeSettingsRepository()
        val backup = FakeBackupManager(db, db.store, settings)
        val updates = FakeUpdateManager(hasUpdateResult = false)
        val reminders = FakeReminderManager()
        val repo = BookmarkRepositoryImpl(db.store.bookmarkDao, db.store.collectionDao, db.store.tagDao)

        val futureTime = System.currentTimeMillis() + 60_000L
        val t1 = db.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://t1.dev", title = "T1", description = null, imageUrl = null, isHidden = true, reminderTime = futureTime)
        )
        val t2 = db.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://t2.dev", title = "T2", description = null, imageUrl = null, isHidden = true)
        )

        val vm = SettingViewModel(
            settingsRepository = settings,
            backupManager = backup,
            updateManager = updates,
            changelogLoader = FakeChangelogLoader("Changelog body"),
            database = db,
            bookmarkRepository = repo,
            reminderManager = reminders,
            ioDispatcher = mainDispatcherRule.dispatcher
        )

        vm.onEvent(SettingEvents.LoadTrash)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, vm.state.value.trashedBookmarks.size)

        vm.onEvent(SettingEvents.RestoreTrashItem(t1))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertFalse(db.store.bookmarkDao.getBookmarkById(t1)!!.isHidden)
        assertEquals(listOf(t1 to futureTime), reminders.scheduled)

        vm.onEvent(SettingEvents.ConfirmEmptyTrash)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertNull(db.store.bookmarkDao.getBookmarkById(t2))
        assertTrue(reminders.cancelled.contains(t2))

        vm.onEvent(SettingEvents.ShowChangelogSheet)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Changelog body", vm.state.value.changelogText)

        vm.onEvent(SettingEvents.CheckForUpdates)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, updates.forceCheckCalls)
        assertTrue(vm.state.value.showNoUpdateToast)
    }

    @Test
    fun testSettingViewModel_exportImportBackupAndErrorHandling() = runTest {
        val db = FakeBookmarkDatabase()
        val settings = FakeSettingsRepository()
        val backup = FakeBackupManager(db, db.store, settings).apply {
            jsonToExport = """{"bookmarks":[]}"""
            htmlToExport = "<!DOCTYPE NETSCAPE-Bookmark-file-1>"
        }
        val repo = BookmarkRepositoryImpl(db.store.bookmarkDao, db.store.collectionDao, db.store.tagDao)
        val vm = SettingViewModel(
            settingsRepository = settings,
            backupManager = backup,
            updateManager = FakeUpdateManager(),
            changelogLoader = FakeChangelogLoader(),
            database = db,
            bookmarkRepository = repo,
            reminderManager = FakeReminderManager(),
            ioDispatcher = mainDispatcherRule.dispatcher
        )

        vm.onEvent(SettingEvents.ExportData)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.exportState is `in`.iambhvsh.shelf.features.setting.ExportState.Ready)
        vm.onEvent(SettingEvents.DismissExport)

        vm.onEvent(SettingEvents.ExportBrowserBookmarks)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.browserExportState is `in`.iambhvsh.shelf.features.setting.ExportState.Ready)
        vm.onEvent(SettingEvents.DismissBrowserExport)

        backup.jsonImportError = IllegalArgumentException("Corrupt JSON")
        vm.onEvent(SettingEvents.ImportData("bad"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.importState is `in`.iambhvsh.shelf.features.setting.ImportState.Error)

        backup.jsonImportError = null
        vm.onEvent(SettingEvents.ImportData("""{"bookmarks":[{"url":"https://imported.dev","title":"Imp"}]}"""))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.importState is `in`.iambhvsh.shelf.features.setting.ImportState.Success)
        assertTrue(backup.refreshThumbnailsCalls > 0)

        vm.onEvent(SettingEvents.ConfirmAutoBackupEnable)
        assertTrue(vm.state.value.autoBackupEnabled)
        assertTrue(backup.autoBackupStarted)

        vm.onEvent(SettingEvents.HidePermissionsSheet)
        assertTrue(settings.firstLaunchPermissionsShown)
    }

    @Test
    fun testSettingViewModel_everyUiSheetToggleExportBrowserAndErrorPaths() = runTest {
        val db = FakeBookmarkDatabase()
        val store = db.store
        val failingRepo = object : `in`.iambhvsh.shelf.data.BookmarkRepository by BookmarkRepositoryImpl(
            store.bookmarkDao,
            store.collectionDao,
            store.tagDao
        ) {
            var failTrashOps = false
            override suspend fun unhideBookmarks(ids: List<Long>) {
                if (failTrashOps) throw IllegalStateException("Unhide fail")
                store.bookmarkDao.unhideBookmarks(ids)
            }

            override suspend fun permanentlyDeleteBookmarks(ids: List<Long>) {
                if (failTrashOps) throw IllegalStateException("Delete fail")
                store.bookmarkDao.deleteBookmarksPermanently(ids)
            }

            override suspend fun emptyTrash() {
                if (failTrashOps) throw IllegalStateException("Empty fail")
                store.bookmarkDao.emptyTrashBookmarks()
            }
        }
        val settings = FakeSettingsRepository()
        val backup = FakeBackupManager(db, store, settings)
        val updates = FakeUpdateManager(hasUpdateResult = false)
        var changelogLoads = 0
        val changelog = object : `in`.iambhvsh.shelf.features.setting.ChangelogLoader(FakeContext()) {
            override fun load(): String {
                changelogLoads++
                return "## 2.0.5\nFixed bugs"
            }
        }
        val reminders = FakeReminderManager()
        val vm = SettingViewModel(
            settingsRepository = settings,
            backupManager = backup,
            updateManager = updates,
            changelogLoader = changelog,
            database = db,
            bookmarkRepository = failingRepo,
            reminderManager = reminders,
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(SettingEvents.ShowThemeSheet)
        assertTrue(vm.state.value.showThemeSheet)
        vm.onEvent(SettingEvents.SelectTheme(ThemeMode.OLED))
        assertEquals(ThemeMode.OLED, vm.state.value.themeMode)
        assertFalse(vm.state.value.showThemeSheet)
        vm.onEvent(SettingEvents.HideThemeSheet)

        vm.onEvent(SettingEvents.ShowTapActionSheet)
        assertTrue(vm.state.value.showTapActionSheet)
        vm.onEvent(SettingEvents.SelectTapAction(TapAction.OPEN_BROWSER))
        assertEquals(TapAction.OPEN_BROWSER, vm.state.value.tapAction)
        assertFalse(vm.state.value.showTapActionSheet)
        vm.onEvent(SettingEvents.HideTapActionSheet)

        vm.onEvent(SettingEvents.ToggleDynamicColor(false))
        assertFalse(vm.state.value.dynamicColor)
        vm.onEvent(SettingEvents.ShowAccentColorSheet)
        assertTrue(vm.state.value.showAccentColorSheet)
        vm.onEvent(SettingEvents.SelectAccentColor(AccentColor.TEAL))
        assertEquals(AccentColor.TEAL, vm.state.value.accentColor)
        assertFalse(vm.state.value.showAccentColorSheet)
        vm.onEvent(SettingEvents.HideAccentColorSheet)

        vm.onEvent(SettingEvents.ShowViewModeSheet)
        assertTrue(vm.state.value.showViewModeSheet)
        vm.onEvent(SettingEvents.ToggleViewMode(ViewMode.LIST))
        assertEquals(ViewMode.LIST, vm.state.value.viewMode)
        assertFalse(vm.state.value.showViewModeSheet)
        vm.onEvent(SettingEvents.HideViewModeSheet)

        vm.onEvent(SettingEvents.ToggleAutoBackup(true))
        assertTrue(vm.state.value.showAutoBackupInfoDialog)
        vm.onEvent(SettingEvents.DismissAutoBackupInfoDialog)
        assertFalse(vm.state.value.showAutoBackupInfoDialog)
        vm.onEvent(SettingEvents.ConfirmAutoBackupEnable)
        vm.onEvent(SettingEvents.SetAutoBackupUri("content://tree/shelf"))
        assertEquals("content://tree/shelf", vm.state.value.autoBackupUri)
        vm.onEvent(SettingEvents.ToggleAutoBackup(false))
        assertFalse(vm.state.value.autoBackupEnabled)
        assertEquals("", vm.state.value.lastBackupTimeText)

        vm.onEvent(SettingEvents.ToggleAppLockUsePin(true))
        assertTrue(vm.state.value.appLockUsePinEnabled)
        vm.onEvent(SettingEvents.ShowAppLockTimeoutSheet)
        assertTrue(vm.state.value.showAppLockTimeoutSheet)
        vm.onEvent(SettingEvents.SetAppLockTimeout(300_000L))
        assertEquals(300_000L, vm.state.value.appLockTimeout)
        assertFalse(vm.state.value.showAppLockTimeoutSheet)
        vm.onEvent(SettingEvents.HideAppLockTimeoutSheet)

        vm.onEvent(SettingEvents.ShowExportSheet)
        assertTrue(vm.state.value.showExportSheet)
        vm.onEvent(SettingEvents.HideExportSheet)
        vm.onEvent(SettingEvents.ShowImportSheet)
        assertTrue(vm.state.value.showImportSheet)
        vm.onEvent(SettingEvents.HideImportSheet)
        vm.onEvent(SettingEvents.ShowAboutSheet)
        assertTrue(vm.state.value.showAboutSheet)
        vm.onEvent(SettingEvents.HideAboutSheet)
        vm.onEvent(SettingEvents.ShowAboutDialog)
        assertTrue(vm.state.value.showAboutDialog)
        vm.onEvent(SettingEvents.HideAboutDialog)
        vm.onEvent(SettingEvents.ShowPermissionsSheet)
        assertTrue(vm.state.value.showPermissionsSheet)

        vm.onEvent(SettingEvents.ShowChangelogSheet)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("## 2.0.5\nFixed bugs", vm.state.value.changelogText)
        vm.onEvent(SettingEvents.HideChangelogSheet)
        vm.onEvent(SettingEvents.ShowChangelogSheet)
        assertEquals(1, changelogLoads)

        vm.onEvent(SettingEvents.CheckForUpdates)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.showNoUpdateToast)
        vm.onEvent(SettingEvents.ResetNoUpdateToast)
        assertFalse(vm.state.value.showNoUpdateToast)

        updates.hasUpdateResult = true
        vm.onEvent(SettingEvents.CheckForUpdates)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.showUpdateSheet)
        vm.onEvent(SettingEvents.InstallUpdate)
        assertEquals(1, updates.downloadAndInstallCalls)
        assertFalse(vm.state.value.showUpdateSheet)
        vm.onEvent(SettingEvents.DismissUpdateSheet)

        vm.onEvent(SettingEvents.ShowResetDialog)
        assertTrue(vm.state.value.showResetDialog)
        vm.onEvent(SettingEvents.HideResetDialog)
        assertFalse(vm.state.value.showResetDialog)
        vm.onEvent(SettingEvents.ResetSuccessToastShown)
        assertFalse(vm.state.value.resetSuccessToast)

        vm.onEvent(SettingEvents.ExportBrowserBookmarks)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.browserExportState is `in`.iambhvsh.shelf.features.setting.ExportState.Ready)
        vm.onEvent(SettingEvents.DismissBrowserExport)
        assertTrue(vm.state.value.browserExportState is `in`.iambhvsh.shelf.features.setting.ExportState.Idle)

        backup.browserImportError = IllegalArgumentException("Bad HTML")
        vm.onEvent(SettingEvents.ImportBrowserBookmarks("<html>"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.browserImportState is `in`.iambhvsh.shelf.features.setting.BrowserImportState.Error)

        backup.browserImportError = null
        vm.onEvent(SettingEvents.ImportBrowserBookmarks("""<DL><p><DT><A HREF="https://x.dev">X</A></DL><p>"""))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.browserImportState is `in`.iambhvsh.shelf.features.setting.BrowserImportState.Success)
        vm.onEvent(SettingEvents.DismissBrowserImportResult)
        assertTrue(vm.state.value.browserImportState is `in`.iambhvsh.shelf.features.setting.BrowserImportState.Idle)

        vm.onEvent(SettingEvents.DismissExport)
        vm.onEvent(SettingEvents.DismissImportResult)

        val tPast = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://past.dev", title = "Past", description = null, imageUrl = null, isHidden = true, reminderTime = 1_000L)
        )
        vm.onEvent(SettingEvents.LoadTrash)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(SettingEvents.ToggleTrashSelection(tPast))
        assertTrue(vm.state.value.selectedTrashIds.contains(tPast))
        vm.onEvent(SettingEvents.ClearTrashSelection)
        assertTrue(vm.state.value.selectedTrashIds.isEmpty())

        failingRepo.failTrashOps = true
        vm.onEvent(SettingEvents.RestoreTrashItem(tPast))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Restore failed", vm.state.value.trashToastMessage)
        vm.onEvent(SettingEvents.DeleteTrashItem(tPast))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Delete failed", vm.state.value.trashToastMessage)
        vm.onEvent(SettingEvents.SelectAllTrash)
        vm.onEvent(SettingEvents.RestoreSelectedTrash)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Restore failed", vm.state.value.trashToastMessage)
        vm.onEvent(SettingEvents.ShowDeletePermanentlyDialog)
        assertTrue(vm.state.value.showDeletePermanentlyDialog)
        vm.onEvent(SettingEvents.HideDeletePermanentlyDialog)
        vm.onEvent(SettingEvents.ConfirmDeletePermanently)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Delete failed", vm.state.value.trashToastMessage)
        vm.onEvent(SettingEvents.ShowEmptyTrashDialog)
        assertTrue(vm.state.value.showEmptyTrashDialog)
        vm.onEvent(SettingEvents.HideEmptyTrashDialog)
        vm.onEvent(SettingEvents.ConfirmEmptyTrash)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Failed to empty trash", vm.state.value.trashToastMessage)

        failingRepo.failTrashOps = false
        vm.onEvent(SettingEvents.RestoreTrashItem(tPast))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertFalse(store.bookmarkDao.getBookmarkById(tPast)!!.isHidden)
        assertTrue(reminders.scheduled.none { it.first == tPast })
        vm.onEvent(SettingEvents.ClearTrashToast)
        assertNull(vm.state.value.trashToastMessage)

        settings.setAppLockEnabled(true)
        settings.setAppLockUsePinEnabled(true)
        settings.setAppLockTimeout(60_000L)
        vm.onEvent(SettingEvents.SyncSecurityState)
        assertTrue(vm.state.value.appLockEnabled)
        assertTrue(vm.state.value.appLockUsePinEnabled)
        assertEquals(60_000L, vm.state.value.appLockTimeout)

        settings.setAppLockEnabled(false)
        vm.onEvent(SettingEvents.SyncSecurityState)
        assertFalse(vm.state.value.appLockEnabled)
    }

    @Test
    fun testSettingViewModel_emptyTrashWithoutLoadCancelsRemindersAndResetClearsBackup() = runTest {
        val db = FakeBookmarkDatabase()
        val store = db.store
        val repo = ControllableBookmarkRepository(store)
        val settingsRepo = FakeSettingsRepository()
        val reminders = FakeReminderManager()
        val backup = FakeBackupManager(db, store, settingsRepo)

        val trashedId = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(
                url = "https://unloaded-trash.dev",
                title = "Unloaded Trash",
                description = null,
                imageUrl = null,
                isHidden = true,
                reminderTime = 999_999_999L
            )
        )
        val pastRestoreId = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(
                url = "https://expired-restore.dev",
                title = "Expired",
                description = null,
                imageUrl = null,
                isHidden = true,
                reminderTime = 1_000L
            )
        )

        val vm = SettingViewModel(
            settingsRepository = settingsRepo,
            backupManager = backup,
            updateManager = FakeUpdateManager(),
            changelogLoader = FakeChangelogLoader(),
            database = db,
            bookmarkRepository = repo,
            reminderManager = reminders,
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(SettingEvents.RestoreTrashItem(pastRestoreId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertNull(store.bookmarkDao.getBookmarkById(pastRestoreId)?.reminderTime)

        vm.onEvent(SettingEvents.ConfirmEmptyTrash)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(reminders.cancelled.contains(trashedId))
        assertNull(store.bookmarkDao.getBookmarkById(trashedId))

        vm.onEvent(SettingEvents.ConfirmReset)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(backup.autoBackupStopped)
        assertTrue(backup.internalBackupCleared)
    }
}
