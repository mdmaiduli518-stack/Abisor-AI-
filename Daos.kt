package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAtMs DESC")
    fun observeAllSessions(): Flow<List<ChatSessionEntity>>

    @Query(
        """
        SELECT DISTINCT s.* FROM chat_sessions s
        LEFT JOIN chat_messages m ON s.id = m.chatId
        WHERE s.title LIKE '%' || :query || '%'
           OR m.text LIKE '%' || :query || '%'
           OR m.attachmentName LIKE '%' || :query || '%'
        ORDER BY s.updatedAtMs DESC
        """
    )
    fun searchSessions(query: String): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions ORDER BY updatedAtMs DESC")
    suspend fun getAllSessionsOnce(): List<ChatSessionEntity>

    @Query("SELECT * FROM chat_sessions WHERE id = :chatId LIMIT 1")
    suspend fun getSessionById(chatId: String): ChatSessionEntity?

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestampMs ASC")
    fun observeMessages(chatId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestampMs ASC")
    suspend fun getMessagesOnce(chatId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: ChatSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_sessions SET title = :title, lastMessagePreview = :preview, updatedAtMs = :updatedAtMs WHERE id = :chatId")
    suspend fun updateSessionSummary(chatId: String, title: String, preview: String, updatedAtMs: Long)

    @Query("UPDATE chat_messages SET text = :text, attachmentPath = :attachmentPath, attachmentType = :attachmentType, isError = :isError WHERE id = :messageId")
    suspend fun updateMessageContent(
        messageId: String,
        text: String,
        attachmentPath: String?,
        attachmentType: String?,
        isError: Boolean
    )

    @Query("DELETE FROM chat_sessions WHERE id = :chatId")
    suspend fun deleteSessionById(chatId: String)

    @Transaction
    @Query("DELETE FROM chat_sessions")
    suspend fun clearAllSessions()
}

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plan_rows ORDER BY sortOrder ASC")
    fun observeAllRows(): Flow<List<StudyPlanRowEntity>>

    @Query("SELECT COUNT(*) FROM study_plan_rows")
    suspend fun countRows(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRow(row: StudyPlanRowEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRows(rows: List<StudyPlanRowEntity>)

    @Query("DELETE FROM study_plan_rows WHERE id = :rowId")
    suspend fun deleteRow(rowId: String)
}
