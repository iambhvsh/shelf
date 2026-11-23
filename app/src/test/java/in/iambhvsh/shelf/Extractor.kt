package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.features.fetch.MetadataExtractor
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class MetadataExtractorTest {

    private val extractor = MetadataExtractor()

    @Test
    fun testExtractTitle_prioritizesOpenGraphTitleOverHtmlTitle() {
        val html = """
            <html>
                <head>
                    <title>Fallback Title</title>
                    <meta property="og:title" content="OpenGraph Premium Title" />
                </head>
                <body></body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        assertEquals("OpenGraph Premium Title", extractor.extractTitle(doc))
    }

    @Test
    fun testExtractTitle_fallsBackToDocTitleWhenOgMissing() {
        val html = """
            <html>
                <head>
                    <title>Standard Fallback Title</title>
                </head>
                <body></body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        assertEquals("Standard Fallback Title", extractor.extractTitle(doc))
    }

    @Test
    fun testExtractDescription_extractsOpenGraphOrMetaDescription() {
        val ogHtml = """
            <html>
                <head>
                    <meta property="og:description" content="This is an OG description." />
                </head>
            </html>
        """.trimIndent()
        assertEquals("This is an OG description.", extractor.extractDescription(Jsoup.parse(ogHtml)))

        val metaHtml = """
            <html>
                <head>
                    <meta name="description" content="This is a standard meta description." />
                </head>
            </html>
        """.trimIndent()
        assertEquals("This is a standard meta description.", extractor.extractDescription(Jsoup.parse(metaHtml)))
    }

    @Test
    fun testExtractImageUrl_prioritizesOgImageAndResolvesAbsolute() {
        val html = """
            <html>
                <head>
                    <meta property="og:image" content="/images/og-preview.png" />
                    <link rel="icon" href="/favicon.ico" />
                </head>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val extracted = extractor.extractImageUrl(doc, "https://shelf.app/features")
        assertEquals("https://shelf.app/images/og-preview.png", extracted)
    }

    @Test
    fun testExtractImageUrl_fallsBackToFaviconWhenNoOgImage() {
        val html = """
            <html>
                <head>
                    <link rel="icon" href="/assets/favicon.png" />
                </head>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val extracted = extractor.extractImageUrl(doc, "https://shelf.app/about")
        assertEquals("https://shelf.app/assets/favicon.png", extracted)
    }
}
