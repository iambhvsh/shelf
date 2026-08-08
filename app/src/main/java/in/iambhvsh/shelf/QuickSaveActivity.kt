package `in`.iambhvsh.shelf

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import `in`.iambhvsh.shelf.domain.model.Bookmark
import `in`.iambhvsh.shelf.domain.repository.BookmarkRepository
import `in`.iambhvsh.shelf.link_fetcher.LinkMetadataParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class QuickSaveActivity : Activity() {
    private val repository: BookmarkRepository by inject()
    private val parser = LinkMetadataParser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        
        if (sharedText.isNullOrEmpty()) {
            Toast.makeText(this, "No valid link", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val url = if (!sharedText.startsWith("http://") && !sharedText.startsWith("https://")) {
            "https://$sharedText"
        } else {
            sharedText
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (repository.existsByUrl(url)) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this@QuickSaveActivity, "Already saved", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val meta = parser.parse(url)
                val bookmark = Bookmark(
                    url = meta?.url ?: url,
                    title = meta?.title,
                    description = meta?.description,
                    imageUrl = meta?.imageUrl
                )
                
                val inserted = repository.insert(bookmark)
                if (inserted) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this@QuickSaveActivity, "Saved", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
            }
        }

        finish()
    }
}
