package `in`.iambhvsh.shelf.features.fetch

class ContentTypeDetector {

    fun detectContentType(url: String): Int {
        val lowerUrl = UrlUtils.decodeUrl(url).lowercase()

        return when {
            videoPatterns.any { it.matchesOrContains(lowerUrl) } -> LinkPreviewType.Companion.VIDEO
            profilePatterns.any { it.matchesOrContains(lowerUrl) } -> LinkPreviewType.Companion.PROFILE
            else -> LinkPreviewType.Companion.WEBPAGE
        }
    }

    private val videoPatterns = listOf(
        "youtube.com/watch",
        "youtu.be/",
        "youtube.com/shorts/",
        "vimeo.com/\\d+".toRegex(),
        "instagram.com/(reel|p/.*/video)".toRegex(),
        "facebook.com/(watch|video)".toRegex(),
        "fb.watch/",
        "twitter.com/.*/status/.*/video".toRegex(),
        "x.com/.*/status/.*/video".toRegex(),
        "twitch.tv/(clip|videos)/".toRegex(),
        "dailymotion.com/video/",
        "v.redd.it/",
        "reddit.com/.*/comments/.*/video".toRegex(),
        "linkedin.com/.*(video|activity-)".toRegex(),
        ".+\\.(mp4|mov|avi|wmv|flv|webm|mkv)(\\?.*)?$".toRegex(),
        "/(video|videos|watch|embed|player|clip|stream)/".toRegex()
    )

    private val profilePatterns = listOf(
        "(?:^|https?://|(?:^|[./]))twitter\\.com/(?!.*status|.*lists).*".toRegex(),
        "(?:^|https?://|(?:^|[./]))x\\.com/(?!.*status|.*lists).*".toRegex(),
        "(?:^|https?://|(?:^|[./]))instagram\\.com/(?!p/|reel/)".toRegex(),
        "(?:^|https?://|(?:^|[./]))facebook\\.com/(?!posts|events|photos)".toRegex(),
        "(?:^|https?://|(?:^|[./]))fb\\.com/(?!posts|events|photos)".toRegex(),
        "(?:^|https?://|(?:^|[./]))linkedin\\.com/(in|profile)/".toRegex(),
        "(?:^|https?://|(?:^|[./]))pinterest\\.com/(?!pin/)".toRegex(),
        "(?:^|https?://|(?:^|[./]))reddit\\.com/(user|u)/".toRegex(),
        "(?:^|https?://|(?:^|[./]))github\\.com/[^/]+/?$".toRegex(),
        "medium.com/@",
        "threads.net/@",
        "(?:^|https?://|(?:^|[./]))twitch\\.tv/[^/]+/?$".toRegex(),
        "wa.me/",
        "chat.whatsapp.com/",
        "/(profile|profiles|user|users|member|members|people)/".toRegex(),
        "/@".toRegex(),
        ".*/~[^/]+/?$".toRegex(),
        "/u/(?!pload/)".toRegex()
    )
}


private fun Any.matchesOrContains(input: String): Boolean = when (this) {
    is Regex -> this.containsMatchIn(input)
    is String -> input.contains(this)
    else -> false
}