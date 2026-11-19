package `in`.iambhvsh.shelf.di

import androidx.room.Room
import `in`.iambhvsh.shelf.backup.BackupManager
import `in`.iambhvsh.shelf.data.BookmarkDatabase
import `in`.iambhvsh.shelf.data.BookmarkRepositoryImpl
import `in`.iambhvsh.shelf.data.SettingsRepositoryImpl
import `in`.iambhvsh.shelf.data.BookmarkRepository
import `in`.iambhvsh.shelf.data.SettingsRepository
import `in`.iambhvsh.shelf.reminder.ReminderManager
import org.koin.android.ext.koin.androidContext
import `in`.iambhvsh.shelf.collection.CollectionViewModel
import `in`.iambhvsh.shelf.home.HomeViewModel
import `in`.iambhvsh.shelf.search.SearchViewModel
import `in`.iambhvsh.shelf.setting.SettingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val shelfModule = module {

    single {
        Room.databaseBuilder(
                get(),
                BookmarkDatabase::class.java,
                "bookmark_db"
            )
            .addMigrations(BookmarkDatabase.MIGRATION_4_5, BookmarkDatabase.MIGRATION_5_6)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single {
        get<BookmarkDatabase>().bookmarkDao()
    }

    single {
        get<BookmarkDatabase>().collectionDao()
    }
    
    single {
        get<BookmarkDatabase>().tagDao()
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    single<BookmarkRepository> {
        BookmarkRepositoryImpl(
            get<BookmarkDatabase>().bookmarkDao(),
            get<BookmarkDatabase>().collectionDao(),
            get<BookmarkDatabase>().tagDao()
        )
    }

    single {
        BackupManager(get(), get(), get(), get(), get())
    }

    single {
        ReminderManager(androidContext())
    }

    single {
        `in`.iambhvsh.shelf.setting.ChangelogLoader(androidContext())
    }

    viewModel {
        HomeViewModel(get(), get(), get())
    }

    viewModel {
        SearchViewModel(get())
    }

    viewModel {
        CollectionViewModel(get())
    }

    viewModel {
        SettingViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        `in`.iambhvsh.shelf.save.QuickSaveViewModel(get())
    }

    single {
        okhttp3.OkHttpClient()
    }

    single<`in`.iambhvsh.shelf.data.UpdateManager> {
        `in`.iambhvsh.shelf.data.UpdateManagerImpl(get(), get(), get())
    }
}
