package ru.vladislavsumin.core.ui.filePicker

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.nio.file.Path
import java.util.UUID

@Composable
actual fun FilePickerDialog(
    mimeType: String,
    onCloseRequest: (result: Path?) -> Unit,
) {
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { fileUri ->
        if (fileUri != null) {
            // TODO переписать эту жесть
            val contentResolver = context.contentResolver
            contentResolver.openInputStream(fileUri).use { inputStream ->
                val name = getFileNameFromUri(context, fileUri)!!
                val ext = name.takeLastWhile { it != '.' }
                val cache = context.cacheDir.resolve("${UUID.randomUUID()}.$ext")
                val bytes = inputStream!!.readAllBytes()
                cache.writeBytes(bytes)
                onCloseRequest(cache.toPath())
            }
        } else {
            onCloseRequest(null)
        }
    }

    LaunchedEffect(filePickerLauncher) {
        filePickerLauncher.launch(mimeType)
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var fileName: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) { // Check if the column exists
                fileName = cursor.getString(nameIndex)
            }
        }
    }
    return fileName
}
