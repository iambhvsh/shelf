package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.BookmarkDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MigrationTest {

    @Test
    fun testMigration1To4_createsRequiredCrossRefIndicesAndClosesCursor() {
        val recorder = RecordingSQLiteDatabase(
            preExistingTables = mapOf(
                "bookmarks" to listOf("id", "url", "title", "description", "imageUrl", "createdAt", "isHidden")
            )
        )

        BookmarkDatabase.MIGRATION_1_4.migrate(recorder.db)

        assertTrue(recorder.queriedCursors.isNotEmpty())
        assertTrue(recorder.queriedCursors.all { it.wasClosed })
        assertTrue(
            recorder.executedSql.any {
                it.contains("CREATE INDEX IF NOT EXISTS `index_bookmark_collection_cross_ref_bookmarkId`")
            }
        )
        assertTrue(
            recorder.executedSql.any {
                it.contains("CREATE INDEX IF NOT EXISTS `index_bookmark_collection_cross_ref_collectionId`")
            }
        )
    }

    @Test
    fun testMigration1To4_upgradesLegacyBookmarksTableMissingIsHiddenColumn() {
        val recorder = RecordingSQLiteDatabase(
            preExistingTables = mapOf(
                "bookmarks" to listOf("id", "url", "title", "description", "imageUrl", "createdAt")
            )
        )

        BookmarkDatabase.MIGRATION_2_4.migrate(recorder.db)

        assertTrue(recorder.executedSql.any { it.contains("ADD COLUMN `isHidden`") })
        assertTrue(recorder.executedSql.any { it.contains("index_bookmark_collection_cross_ref_bookmarkId") })
        assertTrue(recorder.executedSql.any { it.contains("index_bookmark_collection_cross_ref_collectionId") })
    }

    @Test
    fun testFullMigrationChain_coversAllIndicesRequiredByV7SchemaFile() {
        val recorder = RecordingSQLiteDatabase()

        BookmarkDatabase.MIGRATION_3_4.migrate(recorder.db)
        BookmarkDatabase.MIGRATION_4_5.migrate(recorder.db)
        BookmarkDatabase.MIGRATION_5_6.migrate(recorder.db)
        BookmarkDatabase.MIGRATION_6_7.migrate(recorder.db)

        val allSql = recorder.executedSql.joinToString("\n")
        val schemaCandidates = listOf(
            File("schemas/in.iambhvsh.shelf.data.BookmarkDatabase/7.json"),
            File("app/schemas/in.iambhvsh.shelf.data.BookmarkDatabase/7.json"),
            File("app/app/schemas/in.iambhvsh.shelf.data.BookmarkDatabase/7.json")
        )
        val schemaFile = schemaCandidates.first { it.exists() }
        val schemaText = schemaFile.readText()
        val indexRegex = Regex(""""name"\s*:\s*"(index_[^"]+)"""")
        val schemaIndices = indexRegex.findAll(schemaText).map { it.groupValues[1] }.toSet()

        assertTrue(schemaIndices.isNotEmpty())
        for (indexName in schemaIndices) {
            assertTrue("Expected index $indexName in migrations", allSql.contains(indexName))
        }
        assertTrue(allSql.contains("isPinned"))
        assertTrue(allSql.contains("note"))
        assertTrue(allSql.contains("reminderTime"))
    }

    @Test
    fun testAllIndividualMigrations_recordExpectedVersionsAndSql() {
        assertEquals(1, BookmarkDatabase.MIGRATION_1_4.startVersion)
        assertEquals(4, BookmarkDatabase.MIGRATION_1_4.endVersion)
        assertEquals(2, BookmarkDatabase.MIGRATION_2_4.startVersion)
        assertEquals(4, BookmarkDatabase.MIGRATION_2_4.endVersion)
        assertEquals(3, BookmarkDatabase.MIGRATION_3_4.startVersion)
        assertEquals(4, BookmarkDatabase.MIGRATION_3_4.endVersion)
        assertEquals(4, BookmarkDatabase.MIGRATION_4_5.startVersion)
        assertEquals(5, BookmarkDatabase.MIGRATION_4_5.endVersion)
        assertEquals(5, BookmarkDatabase.MIGRATION_5_6.startVersion)
        assertEquals(6, BookmarkDatabase.MIGRATION_5_6.endVersion)
        assertEquals(6, BookmarkDatabase.MIGRATION_6_7.startVersion)
        assertEquals(7, BookmarkDatabase.MIGRATION_6_7.endVersion)

        val db24 = RecordingSQLiteDatabase(
            preExistingTables = mapOf("bookmarks" to listOf("id", "url", "title"))
        )
        BookmarkDatabase.MIGRATION_2_4.migrate(db24.db)
        assertTrue(db24.executedSql.any { it.contains("ADD COLUMN `isHidden`") })

        val db34 = RecordingSQLiteDatabase(
            preExistingTables = mapOf("bookmarks" to listOf("id", "url", "isHidden"))
        )
        BookmarkDatabase.MIGRATION_3_4.migrate(db34.db)
        assertTrue(db34.executedSql.none { it.contains("ADD COLUMN `isHidden`") })

        val db45 = RecordingSQLiteDatabase()
        BookmarkDatabase.MIGRATION_4_5.migrate(db45.db)
        assertTrue(db45.executedSql.any { it.contains("ADD COLUMN isPinned") })
        assertTrue(db45.executedSql.any { it.contains("index_bookmark_tag_cross_ref_tagId") })

        val db56 = RecordingSQLiteDatabase()
        BookmarkDatabase.MIGRATION_5_6.migrate(db56.db)
        assertTrue(db56.executedSql.any { it.contains("ADD COLUMN note") })
        assertTrue(db56.executedSql.any { it.contains("ADD COLUMN reminderTime") })

        val db67 = RecordingSQLiteDatabase()
        BookmarkDatabase.MIGRATION_6_7.migrate(db67.db)
        assertTrue(db67.executedSql.any { it.contains("index_tags_name") })
    }
}
