package com.blackout.app.di

import com.blackout.app.data.repository.QuizRepository
import com.blackout.app.data.repository.QuizRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun quizRepo(impl: QuizRepositoryImpl): QuizRepository
}