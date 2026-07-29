package `in`.iambhvsh.shelf.data.manager

import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import `in`.iambhvsh.shelf.BuildConfig
import `in`.iambhvsh.shelf.domain.manager.UpdateManager
import `in`.iambhvsh.shelf.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

class UpdateManagerImpl(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val okHttpClient: OkHttpClient
) : UpdateManager {

    companion object {
        private const val API_URL = "https://api.github.com/repos/iambhvsh/shelf/releases/latest"
        private const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 hours
        private const val APK_FILE_NAME = "shelf_update.apk"
    }

    private var downloadId: Long = -1

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                installUpdate()
                context.unregisterReceiver(this)
            }
        }
    }

    override suspend fun checkForUpdates(force: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val lastCheckTime = settingsRepository.getLastUpdateCheckTime()

            if (!force && (currentTime - lastCheckTime < CHECK_INTERVAL_MS)) {
                return@withContext settingsRepository.getLatestAvailableVersion() != null
            }

            val request = Request.Builder().url(API_URL).build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                response.body?.string()?.let { responseBody ->
                    val json = JSONObject(responseBody)
                    var tagName = json.getString("tag_name")
                    if (tagName.startsWith("v")) {
                        tagName = tagName.substring(1)
                    }

                    val currentVersion = BuildConfig.VERSION_NAME

                    if (isVersionNewer(currentVersion, tagName)) {
                        val assets = json.getJSONArray("assets")
                        var downloadUrl: String? = null
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            if (asset.getString("name").endsWith(".apk")) {
                                downloadUrl = asset.getString("browser_download_url")
                                break
                            }
                        }

                        if (downloadUrl != null) {
                            settingsRepository.setLatestAvailableVersion(tagName)
                            settingsRepository.setLatestReleaseUrl(downloadUrl)
                            settingsRepository.setLastUpdateCheckTime(currentTime)
                            return@withContext true
                        }
                    } else {
                        settingsRepository.setLatestAvailableVersion(null)
                        settingsRepository.setLatestReleaseUrl(null)
                        settingsRepository.setLastUpdateCheckTime(currentTime)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    override fun downloadAndInstallUpdate() {
        val downloadUrl = settingsRepository.getLatestReleaseUrl() ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    downloadReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(
                    downloadReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }

            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_FILE_NAME)
            if (file.exists()) {
                file.delete()
            }

            val request = DownloadManager.Request(Uri.parse(downloadUrl))
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
            Toast.makeText(context, "Failed to start download.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun installUpdate() {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_FILE_NAME)
        if (!file.exists()) {
            Toast.makeText(context, "Update file not found.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val packageInstaller = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            FileInputStream(file).use { inputStream ->
                session.openWrite("package", 0, -1).use { outputStream ->
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
            session.close()

            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to start installation.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isVersionNewer(current: String, fetched: String): Boolean {
        return try {
            val currentParts = current.split(".").map { it.toInt() }
            val fetchedParts = fetched.split(".").map { it.toInt() }
            
            for (i in 0 until maxOf(currentParts.size, fetchedParts.size)) {
                val c = currentParts.getOrElse(i) { 0 }
                val f = fetchedParts.getOrElse(i) { 0 }
                if (f > c) return true
                if (c > f) return false
            }
            false
        } catch (e: Exception) {
            fetched != current
        }
    }
}
