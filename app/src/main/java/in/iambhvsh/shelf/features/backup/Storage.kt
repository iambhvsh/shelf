package `in`.iambhvsh.shelf.features.backup

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

internal const val BACKUP_FILE_NAME = "shelf_autobackup.json"
internal const val BACKUP_DIR_NAME = "backups"

internal fun getInternalBackupFile(context: Context): File {
    return File(File(context.filesDir, BACKUP_DIR_NAME), BACKUP_FILE_NAME)
}

internal fun readInternalBackupFile(context: Context): String? {
    val file = getInternalBackupFile(context)
    return if (file.exists()) file.readText() else null
}

internal fun writeBackupToStorage(
    context: Context,
    jsonString: String,
    customUriStr: String?
): Long? {
    val internalWritten = runCatching {
        val backupDir = File(context.filesDir, BACKUP_DIR_NAME)
        backupDir.mkdirs()
        val internalFile = File(backupDir, BACKUP_FILE_NAME)
        val tempFile = File(backupDir, "$BACKUP_FILE_NAME.tmp")
        tempFile.writeText(jsonString)
        if (!tempFile.renameTo(internalFile)) {
            internalFile.writeText(jsonString)
            tempFile.delete()
        }
        true
    }.getOrDefault(false)

    val externalSucceeded = when {
        customUriStr != null -> writeToCustomUri(context, jsonString, customUriStr)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> writeToDownloads(context, jsonString)
        else -> true
    }

    return if (internalWritten && externalSucceeded) System.currentTimeMillis() else null
}

private fun writeToCustomUri(context: Context, jsonString: String, uriString: String): Boolean {
    return try {
        val treeUri = android.net.Uri.parse(uriString)
        val dir = DocumentFile.fromTreeUri(context, treeUri)
        if (dir == null || !dir.exists() || !dir.canWrite()) {
            showBackupErrorToast(context)
            return false
        }

        var backupFile = dir.findFile(BACKUP_FILE_NAME)
        if (backupFile == null) {
            backupFile = dir.createFile("application/json", BACKUP_FILE_NAME)
        }

        if (backupFile != null) {
            val stream = context.contentResolver.openOutputStream(backupFile.uri, "wt")
            if (stream != null) {
                stream.use { output ->
                    output.write(jsonString.toByteArray())
                }
                true
            } else {
                showBackupErrorToast(context)
                false
            }
        } else {
            showBackupErrorToast(context)
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        showBackupErrorToast(context)
        false
    }
}

private fun showBackupErrorToast(context: Context) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(
            context,
            "Backup folder unavailable. Please choose a new folder in settings.",
            Toast.LENGTH_LONG
        ).show()
    }
}

@SuppressLint("NewApi")
private fun writeToDownloads(context: Context, jsonString: String): Boolean {
    return try {
        val collectionUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val selection = """
            ${MediaStore.Downloads.DISPLAY_NAME} = ?
            AND
            ${MediaStore.Downloads.RELATIVE_PATH} = ?
        """.trimIndent()
        val selectionArgs = arrayOf(
            BACKUP_FILE_NAME,
            "${Environment.DIRECTORY_DOWNLOADS}/Shelf/"
        )

        var existingUri: android.net.Uri? = null
        context.contentResolver.query(
            collectionUri,
            arrayOf(MediaStore.Downloads._ID),
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                existingUri = ContentUris.withAppendedId(collectionUri, id)
            }
        }

        val uri = existingUri ?: run {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, BACKUP_FILE_NAME)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Shelf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Downloads.IS_PENDING, 0)
                }
            }
            context.contentResolver.insert(collectionUri, contentValues)
        }

        if (uri == null) return false
        val stream = context.contentResolver.openOutputStream(uri, "wt") ?: return false
        stream.use { output ->
            output.write(jsonString.toByteArray())
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
