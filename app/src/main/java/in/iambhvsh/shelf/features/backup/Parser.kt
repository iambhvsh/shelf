package `in`.iambhvsh.shelf.features.backup

import `in`.iambhvsh.shelf.features.fetch.UrlUtils

data class ParsedBookmark(
    val url: String,
    val title: String,
    val collections: List<String> = emptyList()
)

data class BrowserImportResult(
    val imported: Int,
    val skipped: Int,
    val collections: Int
)

class BookmarkParser {

    fun parse(html: String): Pair<List<ParsedBookmark>, List<String>> {
        val normalized = html.replace(Regex(">\\s+<"), "><")

        data class Tag(val position: Int, val type: String, val value: String, val extra: String = "")
        val tags = mutableListOf<Tag>()

        val aRegex = Regex("<A\\s+[^>]*HREF=[\"']([^\"']+)[\"']([^>]*)>(.*?)</A>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val h3Regex = Regex("<H3[^>]*>(.*?)</H3>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val dlEndRegex = Regex("</DL>", RegexOption.IGNORE_CASE)

        for (m in h3Regex.findAll(normalized)) {
            tags.add(Tag(m.range.first, "folder", m.groupValues[1].trim()))
        }
        for (m in aRegex.findAll(normalized)) {
            tags.add(Tag(m.range.first, "bookmark", m.groupValues[1], m.groupValues[3]))
        }
        for (m in dlEndRegex.findAll(normalized)) {
            tags.add(Tag(m.range.first, "close", ""))
        }

        tags.sortBy { it.position }

        val bookmarkMap = linkedMapOf<String, MutableParsedBookmark>()
        val folderStack = ArrayDeque<String?>()
        val collectionsWithBookmarks = linkedSetOf<String>()

        for (tag in tags) {
            when (tag.type) {
                "folder" -> {
                    val name = tag.value.replaceHtmlEntities().trim()
                    if (name.isNotBlank()) {
                        folderStack.addLast(name)
                    } else {
                        folderStack.addLast(null)
                    }
                }
                "bookmark" -> {
                    val url = tag.value.trim().replaceHtmlEntities()
                    val isHttpScheme = url.startsWith("http://", ignoreCase = true) ||
                        url.startsWith("https://", ignoreCase = true)
                    val rawTitle = tag.extra
                        .replace(Regex("<[^>]+>"), "").trim().replaceHtmlEntities()
                    if (url.isNotBlank() && isHttpScheme) {
                        val currentPath = if (folderStack.isNotEmpty()) {
                            folderStack.filterNotNull().filter { it.isNotBlank() }.joinToString("/").ifBlank { null }
                        } else null

                        if (currentPath != null) {
                            collectionsWithBookmarks.add(currentPath)
                        }

                        val key = UrlUtils.normalizeUrlKey(url)
                        val existing = bookmarkMap[key]
                        if (existing == null) {
                            val colList = if (currentPath != null) mutableListOf(currentPath) else mutableListOf()
                            bookmarkMap[key] = MutableParsedBookmark(
                                url = url,
                                title = rawTitle.ifBlank { url },
                                collections = colList
                            )
                        } else {
                            if (currentPath != null && currentPath !in existing.collections) {
                                existing.collections.add(currentPath)
                            }
                            if (existing.title == existing.url && rawTitle.isNotBlank()) {
                                existing.title = rawTitle
                            }
                        }
                    }
                }
                "close" -> {
                    if (folderStack.isNotEmpty()) {
                        folderStack.removeLast()
                    }
                }
            }
        }

        val bookmarks = bookmarkMap.values.map {
            ParsedBookmark(url = it.url, title = it.title, collections = it.collections)
        }
        return Pair(bookmarks, collectionsWithBookmarks.toList())
    }

    private class MutableParsedBookmark(
        val url: String,
        var title: String,
        val collections: MutableList<String>
    )

    private fun String.replaceHtmlEntities(): String {
        val entityRegex = Regex("&(#x[0-9a-fA-F]+|#[0-9]+|amp|lt|gt|quot|apos|nbsp);")
        return entityRegex.replace(this) { match ->
            when (val token = match.groupValues[1]) {
                "amp" -> "&"
                "lt" -> "<"
                "gt" -> ">"
                "quot" -> "\""
                "apos" -> "'"
                "nbsp" -> " "
                else -> {
                    val codePoint = if (token.startsWith("#x")) {
                        token.substring(2).toIntOrNull(16)
                    } else if (token.startsWith("#")) {
                        token.substring(1).toIntOrNull()
                    } else null
                    if (codePoint != null && codePoint in 1..Character.MAX_CODE_POINT && !Character.isSurrogate(codePoint.toChar())) {
                        String(Character.toChars(codePoint))
                    } else {
                        match.value
                    }
                }
            }
        }
    }
}
