package `in`.iambhvsh.shelf.data

import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.database.Cursor
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream

@Serializable
private data class GitHubAsset(
    val name: String = "",
    @SerialName("browser_download_url")
    val browserDownloadUrl: String = ""
)

@Serializable
private data class GitHubRelease(
    @SerialName("tag_name")
    val tagName: String = "",
    val assets: List<GitHubAsset> = emptyList()
)

internal data class UpdateReleaseInfo(
    val version: String,
    val downloadUrl: String
)

class UpdateManagerImpl(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val okHttpClient: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UpdateManager {

    companion object {
        private const val API_URL = "https://api.github.com/repos/iambhvsh/shelf/releases/latest"
        private const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L
        private const val APK_FILE_NAME = "shelf_update.apk"
        private val releaseJson = Json { ignoreUnknownKeys = true }

        internal fun isVersionNewer(current: String, fetched: String): Boolean {
            return try {
                val cleanCurrent = current.trim().removePrefix("v").removePrefix("V").substringBefore("-")
                val cleanFetched = fetched.trim().removePrefix("v").removePrefix("V").substringBefore("-")
                val currentParts = cleanCurrent.split(".").map { it.toIntOrNull() ?: 0 }
                val fetchedParts = cleanFetched.split(".").map { it.toIntOrNull() ?: 0 }

                for (i in 0 until maxOf(currentParts.size, fetchedParts.size)) {
                    val c = currentParts.getOrElse(i) { 0 }
                    val f = fetchedParts.getOrElse(i) { 0 }
                    if (f > c) return true
                    if (c > f) return false
                }
                false
            } catch (_: Exception) {
                false
            }
        }

        internal fun parseLatestRelease(responseBody: String, currentVersion: String): UpdateReleaseInfo? {
            if (responseBody.isBlank()) return null
            val release = runCatching { releaseJson.decodeFromString<GitHubRelease>(responseBody) }.getOrNull()
                ?: return null
            val rawTag = release.tagName.trim()
            if (rawTag.isEmpty()) return null
            val cleanTag = rawTag.removePrefix("v").removePrefix("V")
            if (!isVersionNewer(currentVersion, cleanTag)) return null

            for (asset in release.assets) {
                val url = asset.browserDownloadUrl.trim()
                if (asset.name.endsWith(".apk", ignoreCase = true) &&
                    url.startsWith("https://", ignoreCase = true)
                ) {
                    return UpdateReleaseInfo(version = cleanTag, downloadUrl = url)
                }
            }
            return null
        }

        internal fun isDownloadSuccessful(cursor: Cursor?): Boolean {
            return cursor?.use {
                if (!it.moveToFirst()) return@use false
                val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                statusIndex >= 0 && it.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL
            } ?: false
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var downloadId: Long = -1
    private var isReceiverRegistered = false

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(receiverContext: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                try {
                    context.unregisterReceiver(this)
                } catch (_: Exception) {
                } finally {
                    isReceiverRegistered = false
                }
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        installUpdateSuspending()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    override suspend fun checkForUpdates(force: Boolean): Boolean = withContext(ioDispatcher) {
        try {
            val currentTime = System.currentTimeMillis()
            val lastCheckTime = settingsRepository.getLastUpdateCheckTime()
            val currentVersion = runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
            }.getOrDefault("")

            if (!force && (currentTime - lastCheckTime < CHECK_INTERVAL_MS)) {
                val cachedVersion = settingsRepository.getLatestAvailableVersion()
                val cachedUrl = settingsRepository.getLatestReleaseUrl()
                if (cachedVersion != null && cachedUrl?.startsWith("https://", ignoreCase = true) == true) {
                    if (isVersionNewer(currentVersion, cachedVersion)) {
                        return@withContext true
                    } else {
                        settingsRepository.setLatestAvailableVersion(null)
                        settingsRepository.setLatestReleaseUrl(null)
                        return@withContext false
                    }
                }
                return@withContext false
            }

            val request = Request.Builder().url(API_URL).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext false
                val responseBody = response.body.string()
                val release = parseLatestRelease(responseBody, currentVersion)
                if (release != null) {
                    settingsRepository.setLatestAvailableVersion(release.version)
                    settingsRepository.setLatestReleaseUrl(release.downloadUrl)
                    settingsRepository.setLastUpdateCheckTime(currentTime)
                    return@withContext true
                } else {
                    settingsRepository.setLatestAvailableVersion(null)
                    settingsRepository.setLatestReleaseUrl(null)
                    settingsRepository.setLastUpdateCheckTime(currentTime)
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    override fun downloadAndInstallUpdate() {
        val downloadUrl = settingsRepository.getLatestReleaseUrl()?.trim() ?: return
        if (!downloadUrl.startsWith("https://", ignoreCase = true)) {
            Toast.makeText(context, "Invalid update link.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (!isReceiverRegistered) {
                ContextCompat.registerReceiver(
                    context,
                    downloadReceiver,
                    android.content.IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    ContextCompat.RECEIVER_EXPORTED
                )
                isReceiverRegistered = true
            }

            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_FILE_NAME)
            if (file.exists()) {
                file.delete()
            }

            val request = DownloadManager.Request(downloadUrl.toUri())
                .setTitle("Downloading Shelf Update")
                .setDescription("Downloading the latest version...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, APK_FILE_NAME)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = downloadManager.enqueue(request)
            Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            if (isReceiverRegistered) {
                runCatching { context.unregisterReceiver(downloadReceiver) }
                isReceiverRegistered = false
            }
            Toast.makeText(context, "Failed to start download.", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun installUpdateSuspending() {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (downloadManager == null) {
            showToast("Download manager unavailable.")
            return
        }
        val query = DownloadManager.Query().setFilterById(downloadId)
        val isSuccess = isDownloadSuccessful(downloadManager.query(query))

        if (!isSuccess) {
            showToast("Download failed or was cancelled.")
            return
        }

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_FILE_NAME)
        if (!file.exists()) {
            showToast("Update file not found.")
            return
        }

        var session: PackageInstaller.Session? = null
        var committed = false
        try {
            val packageInstaller = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            val sessionId = packageInstaller.createSession(params)
            session = packageInstaller.openSession(sessionId)

            FileInputStream(file).use { inputStream ->
                session.openWrite("package", 0, file.length()).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    session.fsync(outputStream)
                }
            }

            val intent = Intent(context, InstallationReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
            session.commit(pendingIntent.intentSender)
            committed = true
        } catch (e: Exception) {
            if (!committed) {
                runCatching { session?.abandon() }
            }
            if (e is kotlinx.coroutines.CancellationException) throw e
            e.printStackTrace()
            showToast("Failed to start installation.")
        } finally {
            runCatching { session?.close() }
        }
    }
}
