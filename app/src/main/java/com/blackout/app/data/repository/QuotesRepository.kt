package com.blackout.app.data.repository

import android.content.Context
import com.blackout.app.data.datastore.SettingsDataStore
import com.blackout.app.domain.model.Quote
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuotesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) {
    private val quotes: List<Quote> by lazy { loadQuotesFromAssets() }

    val favoriteQuoteIds: Flow<Set<String>> = settingsDataStore.favoriteQuoteIds

    val favoriteQuotes: Flow<List<Quote>> = settingsDataStore.favoriteQuoteIds.map { ids ->
        quotes.filter { it.id in ids }
    }

    fun getAllQuotes(): List<Quote> = quotes

    fun getRandomQuote(): Quote? = quotes.randomOrNull()

    fun getQuoteById(id: String): Quote? = quotes.find { it.id == id }

    suspend fun toggleFavorite(quoteId: String) {
        settingsDataStore.toggleFavoriteQuote(quoteId)
    }

    private fun loadQuotesFromAssets(): List<Quote> {
        return runCatching {
            val jsonString = context.assets.open("data/quotes.json").use { stream ->
                stream.bufferedReader().use { it.readText() }
            }
            val jsonArray = JSONArray(jsonString)
            val result = mutableListOf<Quote>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val category = obj.optString("category", "general")
                val author = obj.optString("author", "Unknown")
                val textObj = obj.getJSONObject("text")
                val textMap = mutableMapOf<String, String>()
                val keys = textObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    textMap[key] = textObj.getString(key)
                }
                result.add(Quote(id, category, author, textMap))
            }
            result
        }.getOrElse { emptyList() }
    }
}
