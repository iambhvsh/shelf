package `in`.iambhvsh.shelf.features.fetch

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class NetworkFetcher {

    companion object {
        private val client = OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    fun fetchDocument(url: String, agent: String): Document? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", agent)
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val contentType = response.header("Content-Type") ?: ""
                if (!contentType.contains("text/html", ignoreCase = true)) {
                    return null
                }

                val finalUrl = response.request.url.toString()
                
                response.body.byteStream().use { stream ->
                    Jsoup.parse(stream, null, finalUrl)
                }
            }
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
            client.newCall(headRequest).execute().use { response ->
                when (response.code) {
                    in 200..299 -> true
                    405 -> {
                        val rangeRequest = Request.Builder()
                            .url(url)
                            .header("Range", "bytes=0-0")
                            .header("User-Agent", "Mozilla/5.0")
                            .build()
                        client.newCall(rangeRequest).execute().use { it.isSuccessful || it.code == 206 }
                    }
                    in 400..599 -> false
                    else -> response.isSuccessful
                }
            }
        } catch (_: Exception) {
            true
        }
    }
}