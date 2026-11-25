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

    @Test
    fun testExtractImageUrl_coversEveryFallbackTierAndSkipsTinyImages() {
        val docTwitter = Jsoup.parse("""<html><head><meta name="twitter:image" content="//cdn.a.com/tw.jpg"/></head></html>""")
        assertEquals("https://cdn.a.com/tw.jpg", extractor.extractImageUrl(docTwitter, "https://a.com"))

        val docItemProp = Jsoup.parse("""<html><head><meta itemprop="image" content="/item.png"/></head></html>""")
        assertEquals("https://a.com/item.png", extractor.extractImageUrl(docItemProp, "https://a.com/p"))

        val docMetaName = Jsoup.parse("""<html><head><meta name="image" content="pic.png"/></head></html>""")
        assertEquals("https://a.com/blog/pic.png", extractor.extractImageUrl(docMetaName, "https://a.com/blog/post"))

        val docLinkSrc = Jsoup.parse("""<html><head><link rel="image_src" href="/src.png"/></head></html>""")
        assertEquals("https://a.com/src.png", extractor.extractImageUrl(docLinkSrc, "https://a.com"))

        val docApple = Jsoup.parse("""<html><head><link rel="apple-touch-icon" href="/apple.png"/></head></html>""")
        assertEquals("https://a.com/apple.png", extractor.extractImageUrl(docApple, "https://a.com"))

        val docLargeAndTinyImg = Jsoup.parse(
            """
                <html><body>
                    <img src="/tiny.png" width="32" height="32"/>
                    <img src="/nonnum.png" width="auto" height="250"/>
                    <img src="/hero.jpg" width="200" height="200"/>
                </body></html>
            """.trimIndent()
        )
        assertEquals("https://a.com/hero.jpg", extractor.extractImageUrl(docLargeAndTinyImg, "https://a.com"))

        val docEmpty = Jsoup.parse("<html><head></head><body><img src='/small.png' width='199' height='200'/></body></html>")
        assertEquals("", extractor.extractTitle(docEmpty))
        assertEquals("", extractor.extractDescription(docEmpty))
        assertEquals("", extractor.extractImageUrl(docEmpty, "https://a.com"))
    }
}
