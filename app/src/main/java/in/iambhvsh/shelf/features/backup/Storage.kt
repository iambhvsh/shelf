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
): Long {
    val backupDir = File(context.filesDir, BACKUP_DIR_NAME)
    backupDir.mkdirs()
    val internalFile = File(backupDir, BACKUP_FILE_NAME)
    internalFile.writeText(jsonString)

    if (customUriStr != null) {
        writeToCustomUri(context, jsonString, customUriStr)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        writeToDownloads(context, jsonString)
    }

    return System.currentTimeMillis()
}

private fun writeToCustomUri(context: Context, jsonString: String, uriString: String) {
    try {
        val treeUri = android.net.Uri.parse(uriString)
        val dir = DocumentFile.fromTreeUri(context, treeUri)
        if (dir == null || !dir.exists() || !dir.canWrite()) {
            showBackupErrorToast(context)
            return
        }

        var backupFile = dir.findFile(BACKUP_FILE_NAME)
        if (backupFile == null) {
            backupFile = dir.createFile("application/json", BACKUP_FILE_NAME)
        }

        if (backupFile != null) {
            context.contentResolver.openOutputStream(backupFile.uri)?.use { output ->
                output.write(jsonString.toByteArray())
            }
        } else {
            showBackupErrorToast(context)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        showBackupErrorToast(context)
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
private fun writeToDownloads(context: Context, jsonString: String) {
    try {
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

        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { output ->
                output.write(jsonString.toByteArray())
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
