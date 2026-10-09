package com.blackout.app.di

import android.content.Context
import androidx.room.Room
import com.blackout.app.data.local.AppDatabase
import com.blackout.app.data.local.dao.QuizDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun db(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "app.db").build()

    @Provides fun quizDao(db: AppDatabase): QuizDao = db.quizDao()
}

