package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lastMessagePreview: String = "",
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chatId"])]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val role: String, // "me" or "bot"
    val text: String,
    val attachmentPath: String? = null,
    val attachmentName: String? = null,
    val attachmentType: String? = null, // "image", "pdf", "text", "generated_image"
    val isError: Boolean = false,
    val timestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_plan_rows")
data class StudyPlanRowEntity(
    @PrimaryKey val id: String,
    val sortOrder: Int,
    val dateLabel: String,
    val dayOfWeekIso: Int, // 1 (Mon) .. 5 (Fri) .. 7 (Sun)
    val subject: String,
    val chapter: String,
    val hours: String,
    val offDay: Boolean
)
