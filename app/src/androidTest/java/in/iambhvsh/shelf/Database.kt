package `in`.iambhvsh.shelf

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.BookmarkDatabase
import `in`.iambhvsh.shelf.data.BookmarkRepositoryImpl
import `in`.iambhvsh.shelf.util.Resource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseInstrumentedTest {

    @Test
    fun repositoryChunking_handlesOver1100BookmarksOnAndroidSqlite() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BookmarkDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = BookmarkRepositoryImpl(db.bookmarkDao(), db.collectionDao(), db.tagDao())
            val colId = repo.createCollection("BulkCollection")
            val ids = (1..1100).map { idx ->
                repo.saveAndReturnId(
                    Bookmark(
                        url = "https://bulk.example.com/item/$idx",
                        title = "Item $idx",
                        description = null,
                        imageUrl = null
                    )
                )
            }
            assertEquals(1100, ids.size)

            repo.addBookmarksToCollection(ids, colId)
            val inCollection = (repo.getBookmarksInCollection(colId).first { it !is Resource.Loading } as Resource.Success).data!!
            assertEquals(1100, inCollection.size)

            repo.hideBookmarks(ids)
            val trashedCount = repo.getTrashedCount().first()
            assertEquals(1100, trashedCount)

            repo.unhideBookmarks(ids)
            val visibleAfterRestore = (repo.getBookmarks().first { it !is Resource.Loading } as Resource.Success).data!!
            assertEquals(1100, visibleAfterRestore.size)

            repo.permanentlyDeleteBookmarks(ids)
            val remaining = (repo.getBookmarks().first { it !is Resource.Loading } as Resource.Success).data!!
            assertTrue(remaining.isEmpty())
        } finally {
            db.close()
        }
    }

    @Test
    fun migration1To4And6To7_deduplicateTagsAndAddIndicesOnRealSqlite() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration-verify.db"
        context.deleteDatabase(dbName)

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS `bookmarks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT NOT NULL, `title` TEXT, `description` TEXT, `imageUrl` TEXT, `createdAt` INTEGER NOT NULL)")
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )
        try {
            val sqlite = helper.writableDatabase
            BookmarkDatabase.MIGRATION_1_4.migrate(sqlite)
            BookmarkDatabase.MIGRATION_4_5.migrate(sqlite)
            BookmarkDatabase.MIGRATION_5_6.migrate(sqlite)

            sqlite.execSQL("INSERT INTO `bookmarks` (`id`, `url`, `createdAt`) VALUES (1, 'https://a.dev', 100)")
            sqlite.execSQL("INSERT INTO `tags` (`id`, `name`) VALUES (10, 'Kotlin'), (20, 'Kotlin')")
            sqlite.execSQL("INSERT INTO `bookmark_tag_cross_ref` (`bookmarkId`, `tagId`) VALUES (1, 10), (1, 20)")

            BookmarkDatabase.MIGRATION_6_7.migrate(sqlite)

            sqlite.query("SELECT COUNT(*) FROM `tags` WHERE `name` = 'Kotlin'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
            }
            sqlite.query("SELECT `tagId` FROM `bookmark_tag_cross_ref` WHERE `bookmarkId` = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(10, cursor.getInt(0))
                assertFalse(cursor.moveToNext())
            }
            sqlite.query("PRAGMA index_list(`bookmark_collection_cross_ref`)").use { cursor ->
                val names = mutableListOf<String>()
                val nameCol = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) names.add(cursor.getString(nameCol))
                assertTrue(names.contains("index_bookmark_collection_cross_ref_bookmarkId"))
                assertTrue(names.contains("index_bookmark_collection_cross_ref_collectionId"))
            }
        } finally {
            helper.close()
            context.deleteDatabase(dbName)
        }
    }
}
