package com.blackout.app.data.repository

import com.blackout.app.data.local.dao.QuizDao
import com.blackout.app.data.local.entity.JournalEntryEntity
import com.blackout.app.data.local.entity.QuizAnswerEntity
import com.blackout.app.data.local.entity.QuizResultEntity
import com.blackout.app.domain.QuestionBank
import com.blackout.app.domain.model.Answer
import com.blackout.app.domain.model.Question
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import kotlin.collections.filterValues
import kotlin.collections.mapNotNull

interface QuizRepository {
    fun getQuestions(): List<Question>
    suspend fun saveResult(date: LocalDate, answers: Map<String, Answer>)
    fun observeResults(): Flow<List<QuizResultEntity>>
}

class QuizRepositoryImpl @Inject constructor(
    private val dao: QuizDao,
) : QuizRepository {

    override fun getQuestions() = QuestionBank.questions

    override suspend fun saveResult(date: LocalDate, answers: Map<String, Answer>) {
        val answerRows = answers.filterValues { it is Answer.Choice }.map { (qid, a) ->
            val c = a as Answer.Choice
            QuizAnswerEntity(date, qid, c.value, c.points)
        }
        val journalRows = answers.mapNotNull { (qid, a) ->
            (a as? Answer.Text)?.text?.trim()?.takeIf { it.isNotEmpty() }?.let {
                JournalEntryEntity(date = date, source = "quiz", questionId = qid, text = it)
            }
        }
        val score = answers.values.sumOf { it.points }
        dao.saveQuiz(QuizResultEntity(date, score), answerRows, journalRows)
    }

    override fun observeResults() = dao.observeResults()
}