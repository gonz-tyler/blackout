package com.blackout.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.blackout.app.data.local.entity.JournalEntryEntity
import com.blackout.app.data.local.entity.QuizAnswerEntity
import com.blackout.app.data.local.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// data/local/QuizDao.kt
@Dao
interface QuizDao {
    @Upsert
    suspend fun upsertResult(result: QuizResultEntity)
    @Upsert suspend fun upsertAnswers(answers: List<QuizAnswerEntity>)

    @Query("DELETE FROM journal_entries WHERE date = :date AND source = 'quiz'")
    suspend fun clearQuizJournal(date: LocalDate)

    @Insert
    suspend fun insertJournal(entries: List<JournalEntryEntity>)

    @Transaction
    suspend fun saveQuiz(
        result: QuizResultEntity,
        answers: List<QuizAnswerEntity>,
        journal: List<JournalEntryEntity>,
    ) {
        upsertResult(result)
        upsertAnswers(answers)
        clearQuizJournal(result.date)   // avoid duplicates on retake
        insertJournal(journal)
    }

    @Query("SELECT * FROM quiz_results ORDER BY date")
    fun observeResults(): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE date = :date")
    suspend fun resultFor(date: LocalDate): QuizResultEntity?
}