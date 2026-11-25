package `in`.iambhvsh.shelf.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import `in`.iambhvsh.shelf.data.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.dao.CollectionDao
import `in`.iambhvsh.shelf.data.dao.TagDao

@Database(
    entities = [
        BookmarkEntity::class, 
        CollectionEntity::class, 
        BookmarkCollectionCrossRef::class,
        TagEntity::class,
        BookmarkTagCrossRef::class
    ],
    version = 7,
    exportSchema = true
)
abstract class BookmarkDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun collectionDao(): CollectionDao
    abstract fun tagDao(): TagDao
    
    companion object {
        private fun hasColumn(db: SupportSQLiteDatabase, table: String, column: String): Boolean {
            db.query("PRAGMA table_info(`$table`)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                if (nameIndex < 0) return false
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIndex) == column) return true
                }
            }
            return false
        }

        val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `bookmarks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT NOT NULL, `title` TEXT, `description` TEXT, `imageUrl` TEXT, `createdAt` INTEGER NOT NULL, `isHidden` INTEGER NOT NULL DEFAULT 0)")
                if (!hasColumn(db, "bookmarks", "isHidden")) {
                    db.execSQL("ALTER TABLE `bookmarks` ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                }
                db.execSQL("CREATE TABLE IF NOT EXISTS `collections` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `bookmark_collection_cross_ref` (`bookmarkId` INTEGER NOT NULL, `collectionId` INTEGER NOT NULL, PRIMARY KEY(`bookmarkId`, `collectionId`), FOREIGN KEY(`bookmarkId`) REFERENCES `bookmarks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`collectionId`) REFERENCES `collections`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmark_collection_cross_ref_bookmarkId` ON `bookmark_collection_cross_ref` (`bookmarkId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmark_collection_cross_ref_collectionId` ON `bookmark_collection_cross_ref` (`collectionId`)")
            }
        }

        val MIGRATION_2_4 = object : Migration(2, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_4.migrate(db)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_4.migrate(db)
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS `tags` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `bookmark_tag_cross_ref` (`bookmarkId` INTEGER NOT NULL, `tagId` INTEGER NOT NULL, PRIMARY KEY(`bookmarkId`, `tagId`), FOREIGN KEY(`bookmarkId`) REFERENCES `bookmarks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`tagId`) REFERENCES `tags`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmark_tag_cross_ref_bookmarkId` ON `bookmark_tag_cross_ref` (`bookmarkId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmark_tag_cross_ref_tagId` ON `bookmark_tag_cross_ref` (`tagId`)")
            }
        }
        
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN note TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN reminderTime INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    UPDATE OR IGNORE bookmark_tag_cross_ref 
                    SET tagId = (
                        SELECT MIN(t2.id) 
                        FROM tags t2 
                        WHERE t2.name = (SELECT t1.name FROM tags t1 WHERE t1.id = bookmark_tag_cross_ref.tagId)
                    )
                    WHERE tagId NOT IN (SELECT MIN(id) FROM tags GROUP BY name)
                """.trimIndent())
                db.execSQL("DELETE FROM bookmark_tag_cross_ref WHERE tagId NOT IN (SELECT MIN(id) FROM tags GROUP BY name)")
                db.execSQL("DELETE FROM tags WHERE id NOT IN (SELECT MIN(id) FROM tags GROUP BY name)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_tags_name` ON `tags` (`name`)")
            }
        }
    }
}