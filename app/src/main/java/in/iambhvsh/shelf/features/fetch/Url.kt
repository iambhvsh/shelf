package `in`.iambhvsh.shelf.features.fetch

import java.net.URI
import java.net.URL
import java.net.URLDecoder

object UrlUtils {
    fun makeAbsoluteUrl(url: String, baseUrl: String): String = try {
        val trimmed = url.trim()
        when {
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            trimmed.startsWith("//") -> "${URI.create(baseUrl).scheme}:$trimmed"
            trimmed.startsWith("/") -> "${getBaseUrl(baseUrl).trimEnd('/')}$trimmed"
            else -> {
                val baseUri = URI.create(baseUrl)
                val basePath = baseUri.path.substringBeforeLast('/', "")
                "${baseUri.scheme}://${baseUri.authority}$basePath/$trimmed"
            }
        }
    } catch (_: Exception) {
        url
    }

    private fun getBaseUrl(urlString: String): String {
        val url: URL = URI.create(urlString).toURL()
        return url.protocol.toString() + "://" + url.authority + "/"
    }

    fun decodeUrl(url: String): String {
        return runCatching { URLDecoder.decode(url, "UTF-8") }.getOrElse { url }
    }

    fun ensureHttpScheme(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return ""
        val cleaned = normalizeSchemeCasing(trimmed)
        return if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            cleaned
        } else {
            "https://$cleaned"
        }
    }

    fun normalizeUrlKey(url: String): String {
        val normalized = normalizeSchemeCasing(url.trim())
        return normalized.trimEnd('/')
    }

    fun extractUrl(text: String): String {
        val trimmed = text.trim()
        val urlRegex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
        val match = urlRegex.find(trimmed)
        if (match != null) {
            return normalizeSchemeCasing(trimTrailingUrlPunctuation(match.value))
        }
        val fallback = trimTrailingUrlPunctuation(trimmed)
        return ensureHttpScheme(fallback)
    }

    private fun normalizeSchemeCasing(url: String): String {
        return when {
            url.startsWith("https://", ignoreCase = true) -> "https://" + url.substring(8)
            url.startsWith("http://", ignoreCase = true) -> "http://" + url.substring(7)
            else -> url
        }
    }

    private fun trimTrailingUrlPunctuation(raw: String): String {
        var s = raw
        while (s.isNotEmpty()) {
            val last = s.last()
            when {
                last == '.' || last == ',' || last == ';' || last == '!' || last == '?' || last == ']' -> {
                    s = s.dropLast(1)
                }
                last == ')' -> {
                    val opens = s.count { it == '(' }
                    val closes = s.count { it == ')' }
                    if (closes > opens) {
                        s = s.dropLast(1)
                    } else {
                        break
                    }
                }
                else -> break
            }
        }
        return s
    }
}