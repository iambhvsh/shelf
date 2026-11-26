package `in`.iambhvsh.shelf.features.fetch

import java.net.URI
import java.net.URL
import java.net.URLDecoder

object UrlUtils {
    fun makeAbsoluteUrl(url: String, baseUrl: String): String = try {
        val trimmed = url.trim()
        when {
            trimmed.isEmpty() ||
                trimmed.startsWith("data:", ignoreCase = true) ||
                trimmed.startsWith("blob:", ignoreCase = true) -> trimmed
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            trimmed.startsWith("//") -> "${URI.create(baseUrl).scheme}:$trimmed"
            trimmed.startsWith("/") -> "${getBaseUrl(baseUrl).trimEnd('/')}$trimmed"
            else -> URI.create(baseUrl).resolve(trimmed).toString()
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
        val schemeEnd = normalized.indexOf("://")
        val lowerHost = if (schemeEnd != -1) {
            val hostStart = schemeEnd + 3
            val authorityEnd = normalized.indexOfAny(charArrayOf('/', '?', '#'), hostStart)
                .let { if (it == -1) normalized.length else it }
            normalized.substring(0, hostStart) +
                normalized.substring(hostStart, authorityEnd).lowercase() +
                normalized.substring(authorityEnd)
        } else {
            normalized
        }
        val queryIdx = lowerHost.indexOf('?')
        val normalizedRootQuery = if (queryIdx > 0 && lowerHost[queryIdx - 1] == '/') {
            val schemeSep = lowerHost.indexOf("://")
            val firstPathSlash = if (schemeSep != -1) lowerHost.indexOf('/', schemeSep + 3) else lowerHost.indexOf('/')
            if (firstPathSlash == queryIdx - 1) {
                lowerHost.removeRange(queryIdx - 1, queryIdx)
            } else {
                lowerHost
            }
        } else {
            lowerHost
        }
        return normalizedRootQuery.trimEnd('/')
    }

    fun extractUrl(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""
        val urlRegex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
        val match = urlRegex.find(trimmed)
        if (match != null) {
            return normalizeSchemeCasing(trimTrailingUrlPunctuation(match.value))
        }
        val fallback = trimTrailingUrlPunctuation(trimmed)
        if (fallback.isEmpty() || fallback.any { it.isWhitespace() }) return ""
        val hostPart = fallback
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')
            .substringBefore(':')
        val isLocalhost = hostPart.equals("localhost", ignoreCase = true)
        val labels = hostPart.split('.')
        val isIpv4 = labels.size == 4 && labels.all { it.toIntOrNull() in 0..255 }
        val isDomain = hostPart.contains('.') &&
            labels.all { it.isNotEmpty() && it.any { c -> c.isLetterOrDigit() } } &&
            labels.last().any { it.isLetter() }
        if (!isLocalhost && !isIpv4 && !isDomain) return ""
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
                last == '.' || last == ',' || last == ';' || last == '!' ||
                    last == '?' || last == ']' || last == '"' || last == '\'' || last == '>' -> {
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