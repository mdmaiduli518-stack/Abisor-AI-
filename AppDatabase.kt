package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        StudyPlanRowEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun studyPlanDao(): StudyPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "abisor_ai_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

suspend fun StudyPlanDao.seedInitialWeekIfEmpty(isBengali: Boolean) {
    if (countRows() > 0) return
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd MMM (EEE)", if (isBengali) Locale("bn", "BD") else Locale.ENGLISH)

    val defaultSubjectsBn = listOf(
        "বাংলা প্রথম পত্র" to "গদ্য ও কবিতা",
        "গণিত" to "বীজগণিত ও জ্যামিতি",
        "ইংরেজি" to "Grammar & Writing",
        "বিজ্ঞান / পদার্থ" to "গতি ও বল",
        "বাংলাদেশ ও বিশ্বপরিচয়" to "১৯৭১ মুক্তিযুদ্ধ",
        "তথ্য ও যোগাযোগ প্রযুক্তি" to "সংখ্যা পদ্ধতি",
        "সাপ্তাহিক পুনর্পাঠ" to "পুরনো অধ্যায় রিভিশন"
    )
    val defaultSubjectsEn = listOf(
        "Bangla 1st Paper" to "Prose & Poetry",
        "Mathematics" to "Algebra & Geometry",
        "English" to "Grammar & Writing",
        "Science / Physics" to "Motion & Force",
        "Bangladesh Studies" to "1971 Liberation War",
        "ICT" to "Number Systems",
        "Weekly Review" to "Chapter Revision"
    )

    val rows = (0 until 7).map { offset ->
        val date = today.plusDays(offset.toLong())
        val isFriday = date.dayOfWeek == DayOfWeek.FRIDAY
        val pair = if (isBengali) defaultSubjectsBn[offset] else defaultSubjectsEn[offset]
        StudyPlanRowEntity(
            id = "day_${date}",
            sortOrder = offset,
            dateLabel = date.format(formatter),
            dayOfWeekIso = date.dayOfWeek.value,
            subject = if (isFriday) (if (isBengali) "সাপ্তাহিক ছুটি" else "Weekend Off") else pair.first,
            chapter = if (isFriday) (if (isBengali) "বিশ্রাম ও বিনোদন" else "Rest & Refresh") else pair.second,
            hours = if (isFriday) "0" else "2.5",
            offDay = isFriday
        )
    }
    upsertRows(rows)
}
