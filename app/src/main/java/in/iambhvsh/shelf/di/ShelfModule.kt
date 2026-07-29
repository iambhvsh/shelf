package `in`.iambhvsh.shelf.di

import androidx.room.Room
import `in`.iambhvsh.shelf.data.backup.BackupManager
import `in`.iambhvsh.shelf.data.local.BookmarkDatabase
import `in`.iambhvsh.shelf.data.local.repository.BookmarkRepositoryImpl
import `in`.iambhvsh.shelf.data.local.repository.SettingsRepositoryImpl
import `in`.iambhvsh.shelf.domain.repository.BookmarkRepository
import `in`.iambhvsh.shelf.domain.repository.SettingsRepository
import `in`.iambhvsh.shelf.presentation.reminders.ReminderManager
import org.koin.android.ext.koin.androidContext
import `in`.iambhvsh.shelf.presentation.collection.CollectionViewModel
import `in`.iambhvsh.shelf.presentation.home.HomeViewModel
import `in`.iambhvsh.shelf.presentation.search.SearchViewModel
import `in`.iambhvsh.shelf.presentation.setting.SettingViewModel
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
        BackupManager(get(), get(), get(), get())
    }

    single {
        ReminderManager(androidContext())
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
        SettingViewModel(get(), get(), get())
    }

    single {
        okhttp3.OkHttpClient()
    }

    single<`in`.iambhvsh.shelf.domain.manager.UpdateManager> {
        `in`.iambhvsh.shelf.data.manager.UpdateManagerImpl(get(), get(), get())
    }
}
