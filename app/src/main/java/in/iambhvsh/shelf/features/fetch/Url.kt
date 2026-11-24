package `in`.iambhvsh.shelf.features.fetch

import java.net.URI
import java.net.URL
import java.net.URLDecoder

object UrlUtils {
    fun makeAbsoluteUrl(url: String, baseUrl: String): String = try {
        when {
            url.startsWith("http") -> url
            url.startsWith("//") -> "${URI.create(baseUrl).scheme}:$url"
            url.startsWith("/") -> "${getBaseUrl(baseUrl).trimEnd('/')}$url"
            else                -> {
                val baseUri = URI.create(baseUrl)
                val basePath = baseUri.path.substringBeforeLast('/', "")
                "${baseUri.scheme}://${baseUri.authority}$basePath/$url"
            }
        }
    } catch (e: Exception) {
        url
    }

    private fun getBaseUrl(urlString: String): String {
        val url: URL = URI.create(urlString).toURL()
        return url.protocol.toString() + "://" + url.authority + "/"
    }

    fun decodeUrl(url: String): String {
        return runCatching { URLDecoder.decode(url, "UTF-8") }.getOrElse { url }
    }

    fun extractUrl(text: String): String {
        val trimmed = text.trim()
        val urlRegex = Regex("""https?://[^\s]+""")
        val match = urlRegex.find(trimmed)
        if (match != null) {
            return match.value.trimEnd('.', ',', ';', '!', '?', ')', ']')
        }
        val fallback = trimmed.trimEnd('.', ',', ';', '!', '?', ')', ']')
        return if (!fallback.startsWith("http://") && !fallback.startsWith("https://")) {
            "https://$fallback"
        } else {
            fallback
        }
    }
}