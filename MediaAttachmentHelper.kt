package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.example.data.PendingAiAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

data class ProcessedAttachment(
    val localPath: String?,
    val displayName: String,
    val type: String, // "image", "pdf", "text"
    val aiAttachment: PendingAiAttachment
)

object MediaAttachmentHelper {

    fun createCameraImageUri(context: Context): Pair<Uri, File> {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "cam_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return uri to file
    }

    suspend fun processImageUri(context: Context, uri: Uri): Result<ProcessedAttachment> =
        withContext(Dispatchers.IO) {
            try {
                val displayName = resolveDisplayName(context, uri) ?: "photo_${System.currentTimeMillis()}.jpg"
                val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: return@withContext Result.failure(IllegalStateException("Could not decode image."))

                val rotated = applyExifRotationIfPossible(context, uri, rawBitmap)
                val scaled = scaleBitmapDown(rotated, maxDimension = 1536)

                val attachDir = File(context.filesDir, "attachments").apply { mkdirs() }
                val outFile = File(attachDir, "img_${System.currentTimeMillis()}.jpg")
                FileOutputStream(outFile).use { out ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 88, out)
                }

                Result.success(
                    ProcessedAttachment(
                        localPath = outFile.absolutePath,
                        displayName = displayName,
                        type = "image",
                        aiAttachment = PendingAiAttachment(
                            displayName = displayName,
                            type = "image",
                            bitmap = scaled
                        )
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun processDocumentUri(context: Context, uri: Uri): Result<ProcessedAttachment> =
        withContext(Dispatchers.IO) {
            try {
                val displayName = resolveDisplayName(context, uri) ?: "document"
                val mimeType = context.contentResolver.getType(uri) ?: ""
                val isPdf = mimeType.contains("pdf", ignoreCase = true) ||
                    displayName.endsWith(".pdf", ignoreCase = true)

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext Result.failure(IllegalStateException("Unable to read file."))

                if (bytes.size > 18 * 1024 * 1024) {
                    return@withContext Result.failure(IllegalArgumentException("File exceeds 18MB limit."))
                }

                val attachDir = File(context.filesDir, "attachments").apply { mkdirs() }
                val safeExt = if (isPdf) "pdf" else "txt"
                val outFile = File(attachDir, "doc_${System.currentTimeMillis()}.$safeExt")
                outFile.writeBytes(bytes)

                if (isPdf) {
                    Result.success(
                        ProcessedAttachment(
                            localPath = outFile.absolutePath,
                            displayName = displayName,
                            type = "pdf",
                            aiAttachment = PendingAiAttachment(
                                displayName = displayName,
                                type = "pdf",
                                pdfBytes = bytes
                            )
                        )
                    )
                } else {
                    val text = bytes.toString(Charsets.UTF_8).take(40_000)
                    Result.success(
                        ProcessedAttachment(
                            localPath = outFile.absolutePath,
                            displayName = displayName,
                            type = "text",
                            aiAttachment = PendingAiAttachment(
                                displayName = displayName,
                                type = "text",
                                textContent = text
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun saveGeneratedBitmap(context: Context, bitmap: Bitmap): String =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "generated_images").apply { mkdirs() }
            val file = File(dir, "abisor_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        }

    suspend fun saveImageToGallery(context: Context, filePath: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val srcFile = File(filePath)
                if (!srcFile.exists()) {
                    return@withContext Result.failure(IllegalStateException("Image file missing."))
                }
                val fileName = "abisor_ai_${System.currentTimeMillis()}.png"
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Abisor-AI")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext Result.failure(IllegalStateException("Gallery insert failed."))

                resolver.openOutputStream(uri)?.use { out ->
                    srcFile.inputStream().use { input ->
                        input.copyTo(out)
                    }
                } ?: return@withContext Result.failure(IllegalStateException("Could not write to gallery."))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun resolveDisplayName(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") return uri.lastPathSegment
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
                }
        } catch (_: Exception) {
            null
        }
    }

    private fun applyExifRotationIfPossible(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

            val degrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (degrees == 0f) return bitmap
            val matrix = Matrix().apply { postRotate(degrees) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (_: Exception) {
            bitmap
        }
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val largest = max(bitmap.width, bitmap.height)
        if (largest <= maxDimension) return bitmap
        val ratio = maxDimension.toFloat() / largest.toFloat()
        val newW = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val newH = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }
}
