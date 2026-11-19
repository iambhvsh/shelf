package `in`.iambhvsh.shelf

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.room.Room
import `in`.iambhvsh.shelf.data.BookmarkDatabase
import `in`.iambhvsh.shelf.data.BookmarkRepository
import `in`.iambhvsh.shelf.data.BookmarkRepositoryImpl
import `in`.iambhvsh.shelf.data.SettingsRepository
import `in`.iambhvsh.shelf.data.SettingsRepositoryImpl
import `in`.iambhvsh.shelf.data.UpdateManager
import `in`.iambhvsh.shelf.data.UpdateManagerImpl
import `in`.iambhvsh.shelf.features.backup.BackupManager
import `in`.iambhvsh.shelf.features.collection.CollectionViewModel
import `in`.iambhvsh.shelf.features.home.HomeViewModel
import `in`.iambhvsh.shelf.features.reminder.ReminderManager
import `in`.iambhvsh.shelf.features.save.QuickSaveViewModel
import `in`.iambhvsh.shelf.features.search.SearchViewModel
import `in`.iambhvsh.shelf.features.setting.ChangelogLoader
import `in`.iambhvsh.shelf.features.setting.SettingViewModel
import `in`.iambhvsh.shelf.theme.applyThemeMode
import okhttp3.OkHttpClient
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class App : Application(), DefaultLifecycleObserver {

    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate() {
        super<Application>.onCreate()
        startKoin {
            androidContext(this@App)
            modules(shelfModule)
        }
        applyThemeMode(settingsRepository.getThemeMode(), this)
        GlobalContext.getOrNull()?.get<BackupManager>()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        val appLockEnabled = settingsRepository.getAppLockEnabled()
        val timeout = settingsRepository.getAppLockTimeout()
        SessionManager.onAppForegrounded(appLockEnabled, timeout)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        SessionManager.onAppBackgrounded()
    }
}

val shelfModule = module {
    single {
        Room.databaseBuilder(get(), BookmarkDatabase::class.java, "bookmark_db")
            .addMigrations(BookmarkDatabase.MIGRATION_4_5, BookmarkDatabase.MIGRATION_5_6)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single { get<BookmarkDatabase>().bookmarkDao() }
    single { get<BookmarkDatabase>().collectionDao() }
    single { get<BookmarkDatabase>().tagDao() }

    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    single<BookmarkRepository> {
        BookmarkRepositoryImpl(
            get<BookmarkDatabase>().bookmarkDao(),
            get<BookmarkDatabase>().collectionDao(),
            get<BookmarkDatabase>().tagDao()
        )
    }

    single { BackupManager(get(), get(), get(), get(), get()) }
    single { ReminderManager(androidContext()) }
    single { ChangelogLoader(androidContext()) }
    single { OkHttpClient() }
    single<UpdateManager> { UpdateManagerImpl(get(), get(), get()) }

    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { CollectionViewModel(get()) }
    viewModel { SettingViewModel(get(), get(), get(), get(), get()) }
    viewModel { QuickSaveViewModel(get()) }
}
