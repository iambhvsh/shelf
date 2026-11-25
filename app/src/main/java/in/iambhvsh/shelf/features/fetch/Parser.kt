package `in`.iambhvsh.shelf.features.fetch

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class LinkMetadataParser(
    private val service: LinkPreviewService = LinkPreviewService(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private val AGENTS = arrayOf(
            "WhatsApp/2.24.8.76 A",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36"
        )
    }

    open suspend fun parse(url: String): LinkPreviewData? = withContext(ioDispatcher) {
        try {
            var bestResult: LinkPreviewData? = null
            for (agent in AGENTS) {
                val parsed = service.getLinkPreviewData(url, agent)
                if (bestResult == null && parsed != null) {
                    bestResult = parsed
                }
                if (!checkNullParserResult(parsed)) {
                    return@withContext parsed
                }
            }
            bestResult
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun checkNullParserResult(linkPreviewData: LinkPreviewData?): Boolean {
        if (linkPreviewData == null) return true
        return (linkPreviewData.title.isNullOrEmpty() || linkPreviewData.title == "null") && (linkPreviewData.description.isNullOrEmpty() || linkPreviewData.description == "null")
    }
}

internal suspend fun refreshStaleImage(
    id: Long,
    url: String,
    failedImageUrl: String?,
    parser: LinkMetadataParser,
    attempted: MutableSet<String>,
    updateImageUrl: suspend (Long, String) -> Unit
) {
    val key = "$id:${failedImageUrl ?: ""}"
    val shouldAttempt = synchronized(attempted) { attempted.add(key) }
    if (!shouldAttempt) return
    val meta = parser.parse(url) ?: return
    val newUrl = meta.imageUrl?.trim()
    if (!newUrl.isNullOrBlank() && newUrl != failedImageUrl) {
        updateImageUrl(id, newUrl)
    }
}
