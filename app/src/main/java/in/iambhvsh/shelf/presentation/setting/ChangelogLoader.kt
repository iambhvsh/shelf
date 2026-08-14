package `in`.iambhvsh.shelf.presentation.setting

import android.content.Context

class ChangelogLoader(
    private val context: Context
) {
    fun load(): String {
        return try {
            context.assets
                .open("changelog.md")
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            "Error loading changelog: ${e.message}"
        }
    }
}
