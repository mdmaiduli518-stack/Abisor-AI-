package com.example.data.remote

import android.content.Context
import androidx.annotation.Keep
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.model.DocumentKey
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@Keep
@IgnoreExtraProperties
data class SyncedChatSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val lastMessagePreview: String = "",
    val createdAtMs: Long = 0L,
    val updatedAtMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SyncedChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = "bot",
    val text: String = "",
    val attachmentName: String? = null,
    val attachmentType: String? = null,
    val timestampMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class FirestoreChatSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun safeDocId(raw: String): String {
        val cleaned = raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return cleaned.take(120).ifBlank { "id_${System.currentTimeMillis()}" }
    }

    private fun requireActiveUserId(expectedUid: String? = null): String {
        val uid = auth.currentUser?.uid
        check(!uid.isNullOrBlank()) { "User must be signed in with Google before syncing to Firestore." }
        if (expectedUid != null) {
            check(uid == expectedUid) { "Active user changed during sync." }
        }
        return uid
    }

    fun observeCloudSessions(userId: String): Flow<List<SyncedChatSession>> = flow {
        requireActiveUserId(userId)
        val path = "users/$userId/chats"
        emitAll(
            db.collection("users").document(userId).collection("chats")
                .orderBy("updatedAtMs", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SyncedChatSession::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun fetchCloudMessages(userId: String, chatId: String): Result<List<SyncedChatMessage>> {
        val safeChatId = safeDocId(chatId)
        val path = "users/$userId/chats/$safeChatId/messages"
        return try {
            requireActiveUserId(userId)
            val snap = db.collection("users").document(userId)
                .collection("chats").document(safeChatId)
                .collection("messages")
                .orderBy("timestampMs", Query.Direction.ASCENDING)
                .get()
                .await()
            val msgs = snap.toObjects(
                SyncedChatMessage::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(msgs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun syncSessionAndMessages(
        session: ChatSessionEntity,
        messages: List<ChatMessageEntity>
    ): Result<Unit> {
        val safeChatId = safeDocId(session.id)
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val chatPath = "users/$uid/chats/$safeChatId"
        return try {
            requireActiveUserId(uid)
            val chatRef = db.collection("users").document(uid).collection("chats").document(safeChatId)
            val existingChat = chatRef.get().await()

            val batch = db.batch()
            val cleanTitle = session.title.trim().take(120).ifBlank { "Chat" }
            val cleanPreview = session.lastMessagePreview.trim().take(300)

            if (!existingChat.exists()) {
                val createPayload = mapOf(
                    "userId" to uid,
                    "title" to cleanTitle,
                    "lastMessagePreview" to cleanPreview,
                    "createdAtMs" to session.createdAtMs.coerceAtLeast(0L),
                    "updatedAtMs" to session.updatedAtMs.coerceAtLeast(0L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(chatRef, createPayload)
            } else {
                val updatePayload = mapOf(
                    "title" to cleanTitle,
                    "lastMessagePreview" to cleanPreview,
                    "updatedAtMs" to session.updatedAtMs.coerceAtLeast(0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                batch.update(chatRef, updatePayload)
            }

            messages.filter { !it.isError }.forEach { msg ->
                val safeMsgId = safeDocId(msg.id)
                val msgRef = chatRef.collection("messages").document(safeMsgId)
                val safeRole = if (msg.role == "me") "me" else "bot"
                val safeType = msg.attachmentType?.takeIf {
                    it in setOf("image", "pdf", "text", "generated_image")
                }
                val safeName = msg.attachmentName?.take(255)
                val msgPayload = mapOf(
                    "userId" to uid,
                    "role" to safeRole,
                    "text" to msg.text.take(20000),
                    "attachmentName" to safeName,
                    "attachmentType" to safeType,
                    "timestampMs" to msg.timestampMs.coerceAtLeast(0L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                batch.set(msgRef, msgPayload, SetOptions.merge())
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, chatPath)
            Result.failure(e)
        }
    }

    suspend fun upsertMessage(
        chatId: String,
        message: ChatMessageEntity,
        session: ChatSessionEntity
    ): Result<Unit> {
        if (message.isError) return Result.success(Unit)
        return syncSessionAndMessages(session, listOf(message))
    }

    suspend fun deleteCloudChat(chatId: String): Result<Unit> {
        val safeChatId = safeDocId(chatId)
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val chatPath = "users/$uid/chats/$safeChatId"
        return try {
            requireActiveUserId(uid)
            val chatRef = db.collection("users").document(uid).collection("chats").document(safeChatId)
            val messagesSnap = chatRef.collection("messages").get().await()
            val batch = db.batch()
            messagesSnap.documents.forEach { batch.delete(it.reference) }
            batch.delete(chatRef)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, chatPath)
            Result.failure(e)
        }
    }

    suspend fun clearAllCloudChats(): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.success(Unit)
        val path = "users/$uid/chats"
        return try {
            requireActiveUserId(uid)
            val chatsSnap = db.collection("users").document(uid).collection("chats").get().await()
            for (chatDoc in chatsSnap.documents) {
                val msgSnap = chatDoc.reference.collection("messages").get().await()
                val batch = db.batch()
                msgSnap.documents.forEach { batch.delete(it.reference) }
                batch.delete(chatDoc.reference)
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
