package com.example.data

import android.graphics.Bitmap
import android.util.Log
import com.example.data.local.ChatMessageEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "GeminiAiRepository"

private const val SYSTEM_PROMPT =
    "You are Abisor-AI. You know Bangladesh context well. Reply in simple Bangla. " +
        "Understand Banglish (Bangla written in English letters) and English too."

data class PendingAiAttachment(
    val displayName: String,
    val type: String, // "image", "pdf", "text"
    val bitmap: Bitmap? = null,
    val pdfBytes: ByteArray? = null,
    val textContent: String? = null
)

class GeminiAiRepository {

    private val fastModel by lazy {
        Firebase.ai.generativeModel(
            modelName = ModelTier.FAST.modelName,
            systemInstruction = content { text(SYSTEM_PROMPT) }
        )
    }

    private val smartModel by lazy {
        Firebase.ai.generativeModel(
            modelName = ModelTier.SMART.modelName,
            systemInstruction = content { text(SYSTEM_PROMPT) }
        )
    }

    private val imageGenModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-nano-banana-2.1",
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.IMAGE)
            }
        )
    }

    private fun modelFor(tier: ModelTier) = when (tier) {
        ModelTier.FAST -> fastModel
        ModelTier.SMART -> smartModel
    }

    fun streamReply(
        tier: ModelTier,
        historyMessages: List<ChatMessageEntity>,
        userPrompt: String,
        attachment: PendingAiAttachment?
    ): Flow<String> = flow {
        val model = modelFor(tier)
        // Build multi-turn content list from recent messages
        val contents = mutableListOf<Content>()
        historyMessages
            .filter { !it.isError && it.text.isNotBlank() }
            .takeLast(12)
            .forEach { msg ->
                val role = if (msg.role == "me") "user" else "model"
                contents.add(content(role = role) { text(msg.text) })
            }

        val currentUserContent = content(role = "user") {
            if (attachment != null) {
                when (attachment.type) {
                    "image" -> attachment.bitmap?.let { image(it) }
                    "pdf" -> attachment.pdfBytes?.let { inlineData(it, "application/pdf") }
                    "text" -> attachment.textContent?.let {
                        text("Attached file (${attachment.displayName}):\n$it\n")
                    }
                }
            }
            text(userPrompt)
        }
        contents.add(currentUserContent)

        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            model.generateContentStream(contents).collect { chunk ->
                val piece = chunk.text.orEmpty()
                if (piece.isNotEmpty()) {
                    accumulated.append(piece)
                    emit(accumulated.toString())
                }
            }
        }
    }

    suspend fun generateImage(prompt: String): Result<Bitmap> {
        return try {
            val bitmap = withTimeout(60_000L) {
                val response = imageGenModel.generateContent(prompt)
                response.candidates.firstOrNull()
                    ?.content?.parts
                    ?.filterIsInstance<ImagePart>()
                    ?.firstOrNull()
                    ?.image
            }
            if (bitmap != null) {
                Result.success(bitmap)
            } else {
                Result.failure(IllegalStateException("Model returned no image."))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Image generation timed out", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Image generation failed", e)
            Result.failure(e)
        }
    }
}
