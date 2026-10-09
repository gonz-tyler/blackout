package com.blackout.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.blackout.app.data.local.dao.QuizDao
import com.blackout.app.data.local.entity.JournalEntryEntity
import com.blackout.app.data.local.entity.QuizResultEntity
import com.blackout.app.data.local.entity.QuizAnswerEntity

// data/local/AppDatabase.kt
@Database(
    entities = [QuizResultEntity::class, QuizAnswerEntity::class, JournalEntryEntity::class
        /* , HabitEntity::class, HabitCompletionEntity::class */],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
}