package in.iambhvsh.shelf.di

import androidx.room.Room
import in.iambhvsh.shelf.data.backup.BackupManager
import in.iambhvsh.shelf.data.local.BookmarkDatabase
import in.iambhvsh.shelf.data.local.repository.BookmarkRepositoryImpl
import in.iambhvsh.shelf.data.local.repository.SettingsRepositoryImpl
import in.iambhvsh.shelf.domain.repository.BookmarkRepository
import in.iambhvsh.shelf.domain.repository.SettingsRepository
import in.iambhvsh.shelf.presentation.collection.CollectionViewModel
import in.iambhvsh.shelf.presentation.home.HomeViewModel
import in.iambhvsh.shelf.presentation.search.SearchViewModel
import in.iambhvsh.shelf.presentation.setting.SettingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val shelfModule = module {

    // Database
    single {
        Room.databaseBuilder(
                get(),
                BookmarkDatabase::class.java,
                "bookmark_db"
            )            .fallbackToDestructiveMigration(true)
            .build()
    }

    single {
        get<BookmarkDatabase>().bookmarkDao()
    }

    single {
        get<BookmarkDatabase>().collectionDao()
    }

    // Repository
    single<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    single<BookmarkRepository> {
        BookmarkRepositoryImpl(get(), get())
    }

    single {
        BackupManager(get(), get(), get(), get())
    }

    viewModel {
        HomeViewModel(get())
    }

    viewModel {
        SearchViewModel(get())
    }

    viewModel {
        CollectionViewModel(get())
    }

    viewModel {
        SettingViewModel(get(), get())
    }
}
