package `in`.iambhvsh.shelf.features.fetch

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class NetworkFetcher(
    private val httpClient: OkHttpClient = client
) {

    companion object {
        private const val MAX_HTML_BYTES = 2 * 1024 * 1024

        private val client = OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .callTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    fun fetchDocument(url: String, agent: String): Document? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", agent)
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val contentType = response.header("Content-Type") ?: ""
                val isSupportedHtml = contentType.contains("text/html", ignoreCase = true) ||
                    contentType.contains("application/xhtml+xml", ignoreCase = true)
                if (!isSupportedHtml) {
                    return null
                }

                val finalUrl = response.request.url.toString()
                val charsetName = response.body.contentType()?.charset()?.name()

                val bytes = response.body.byteStream().use { stream ->
                    val out = java.io.ByteArrayOutputStream()
                    val buf = ByteArray(8192)
                    var total = 0
                    while (total < MAX_HTML_BYTES) {
                        val read = stream.read(buf, 0, minOf(buf.size, MAX_HTML_BYTES - total))
                        if (read <= 0) break
                        out.write(buf, 0, read)
                        total += read
                    }
                    out.toByteArray()
                }
                Jsoup.parse(java.io.ByteArrayInputStream(bytes), charsetName, finalUrl)
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isImageUrlReachable(url: String): Boolean {
        if (url.isBlank()) return false
        return try {
            val headRequest = Request.Builder()
                .url(url)
                .head()
                .header("User-Agent", "Mozilla/5.0")
                .build()
            httpClient.newCall(headRequest).execute().use { response ->
                when (response.code) {
                    in 200..299 -> true
                    405 -> {
                        val rangeRequest = Request.Builder()
                            .url(url)
                            .header("Range", "bytes=0-0")
                            .header("User-Agent", "Mozilla/5.0")
                            .build()
                        httpClient.newCall(rangeRequest).execute().use { it.isSuccessful || it.code == 206 }
                    }
                    in 400..599 -> false
                    else -> response.isSuccessful
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            true
        }
    }
}