package `in`.iambhvsh.shelf.features.fetch

import org.jsoup.nodes.Document

class MetadataExtractor {
    private fun Document.metaContent(selector: String): String =
        select(selector).firstOrNull { it.attr("content").isNotBlank() }?.attr("content").orEmpty()

    fun extractTitle(doc: Document) : String{
        return doc.metaContent("meta[property=og:title]").ifBlank { doc.title() }
    }

    fun extractDescription(doc: Document) : String{
        return doc.metaContent("meta[property=og:description]")
            .ifBlank { doc.metaContent("meta[name=description]") }
    }

    fun extractImageUrl(doc: Document, baseUrl: String): String {
        val imageSources = listOf(
            doc.metaContent("meta[property=og:image]"),
            doc.metaContent("meta[property=og:image:secure_url]"),
            doc.metaContent("meta[name=twitter:image]"),
            doc.metaContent("meta[itemprop=image]"),
            doc.metaContent("meta[name=image]"),
            doc.select("link[rel=image_src]").attr("href"),
            doc.select("link[rel=apple-touch-icon]").attr("href"),
            doc.select("link[rel=icon], link[rel='shortcut icon']").attr("href"),
            doc.select("img[src][width][height]").firstOrNull {
                val width = it.attr("width").toIntOrNull() ?: 0
                val height = it.attr("height").toIntOrNull() ?: 0
                width >= 200 && height >= 200
            }?.attr("src").orEmpty()
        )

        return imageSources.firstOrNull { it.isNotBlank() }?.let {
            UrlUtils.makeAbsoluteUrl(it, baseUrl)
        } ?: ""
    }
}