package com.blackout.app.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.blackout.app.domain.service.GoalChange
import com.blackout.app.domain.service.StreakState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

data class UiSettings(
    val seedColor: Int,
    val themeMode: String,       // "light" | "dark" | "system"
    val dynamicColor: Boolean,
    val paletteStyle: String,    // PaletteStyle.name, parsed in the UI layer
    val language: String,
)

/** Per-feature switches. */
data class FeatureToggles(
    val streak: Boolean,
    val yearProgress: Boolean,
)

val DEFAULT_WISDOM_TRADITIONS = setOf("stoic", "buddhist", "biblical", "taoist", "modern")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val DEFAULT_SEED = 0xFFC9A24B.toInt()
        const val DEFAULT_PALETTE_STYLE = "TonalSpot"
        const val LANGUAGE_SYSTEM = "system"
    }

    private object Keys {
        val SEED_COLOR = intPreferencesKey("seed_color_argb")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val PALETTE_STYLE = stringPreferencesKey("palette_style")
        val LANGUAGE = stringPreferencesKey("language_code")
        val FEATURE_STREAK = booleanPreferencesKey("feature_streak")
        val FEATURE_YEAR_PROGRESS = booleanPreferencesKey("feature_year_progress")
        val WISDOM_TRADITIONS = stringSetPreferencesKey("wisdom_traditions")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

        val STREAK_STATE = stringPreferencesKey("streak_state")
        val FAVORITE_QUOTES = stringSetPreferencesKey("favorite_quote_ids")
    }

    private val prefs: Flow<Preferences> = context.dataStore.data.catch {
        if (it is IOException) emit(emptyPreferences()) else throw it
    }

    val uiSettings: Flow<UiSettings> = prefs.map {
        UiSettings(
            seedColor = it.effectiveSeed(),
            themeMode = it[Keys.THEME_MODE] ?: "system",
            dynamicColor = it[Keys.DYNAMIC_COLOR] ?: false,
            paletteStyle = it[Keys.PALETTE_STYLE] ?: DEFAULT_PALETTE_STYLE,
            language = it[Keys.LANGUAGE] ?: LANGUAGE_SYSTEM,
        )
    }.distinctUntilChanged()

    private fun Preferences.streakOn(): Boolean = this[Keys.FEATURE_STREAK] ?: true
    private fun Preferences.yearProgressOn(): Boolean = this[Keys.FEATURE_YEAR_PROGRESS] ?: true

    private fun Preferences.effectiveSeed(): Int =
        this[Keys.SEED_COLOR] ?: 0xFFC9A24B.toInt()

    val streakEnabled: Flow<Boolean> = prefs.map { it.streakOn() }.distinctUntilChanged()
    val yearProgressEnabled: Flow<Boolean> = prefs.map { it.yearProgressOn() }.distinctUntilChanged()

    val wisdomTraditions: Flow<Set<String>> = prefs.map {
        it[Keys.WISDOM_TRADITIONS] ?: DEFAULT_WISDOM_TRADITIONS
    }.distinctUntilChanged()

    val features: Flow<FeatureToggles> = prefs.map {
        FeatureToggles(
            streak = it.streakOn(),
            yearProgress = it.yearProgressOn()
        )
    }.distinctUntilChanged()

    val seedColor: Flow<Int> = prefs.map { it.effectiveSeed() }
    val themeMode: Flow<String> = prefs.map { it[Keys.THEME_MODE] ?: "system" }
    val dynamicColor: Flow<Boolean> = prefs.map { it[Keys.DYNAMIC_COLOR] ?: false }
    val paletteStyle: Flow<String> = prefs.map { it[Keys.PALETTE_STYLE] ?: DEFAULT_PALETTE_STYLE }
    val language: Flow<String> = prefs.map { it[Keys.LANGUAGE] ?: LANGUAGE_SYSTEM }

    val reminderTime: Flow<String> = prefs.map { it[Keys.REMINDER_TIME] ?: "09:00" }
    val notificationsEnabled: Flow<Boolean> = prefs.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }
    val favoriteQuoteIds: Flow<Set<String>> = prefs.map { it[Keys.FAVORITE_QUOTES] ?: emptySet() }.distinctUntilChanged()

    val streakState: Flow<StreakState> = prefs.map {
        it[Keys.STREAK_STATE]?.let(StreakStateJson::decode) ?: StreakState()
    }

    suspend fun setSeedColor(argb: Int) { context.dataStore.edit { it[Keys.SEED_COLOR] = argb } }
    suspend fun setThemeMode(mode: String) { context.dataStore.edit { it[Keys.THEME_MODE] = mode } }
    suspend fun setDynamicColor(enabled: Boolean) { context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled } }
    suspend fun setPaletteStyle(styleName: String) { context.dataStore.edit { it[Keys.PALETTE_STYLE] = styleName } }
    suspend fun setLanguage(code: String) { context.dataStore.edit { it[Keys.LANGUAGE] = code } }

    suspend fun setStreakEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.FEATURE_STREAK] = enabled } }
    suspend fun setYearProgressEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.FEATURE_YEAR_PROGRESS] = enabled } }

    suspend fun toggleWisdomTradition(tradition: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.WISDOM_TRADITIONS] ?: DEFAULT_WISDOM_TRADITIONS
            prefs[Keys.WISDOM_TRADITIONS] = if (enabled) current + tradition else current - tradition
        }
    }

    suspend fun clearSeedColor() { context.dataStore.edit { it.remove(Keys.SEED_COLOR) } }
    suspend fun setReminderTime(time: String) { context.dataStore.edit { it[Keys.REMINDER_TIME] = time } }
    suspend fun setNotificationsEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled } }

    suspend fun toggleFavoriteQuote(quoteId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_QUOTES] ?: emptySet()
            prefs[Keys.FAVORITE_QUOTES] = if (current.contains(quoteId)) current - quoteId else current + quoteId
        }
    }

    suspend fun updateStreakState(transform: (StreakState) -> StreakState) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.STREAK_STATE]?.let(StreakStateJson::decode) ?: StreakState()
            prefs[Keys.STREAK_STATE] = StreakStateJson.encode(transform(current))
        }
    }

    object StreakStateJson {
        fun encode(s: StreakState): String = JSONObject().apply {
            put("start", s.startDate?.toString() ?: JSONObject.NULL)
            put("broken", s.brokenThrough?.toString() ?: JSONObject.NULL)
            put("goals", JSONArray().also { arr ->
                s.goalHistory.forEach {
                    arr.put(JSONObject().put("from", it.effectiveFrom.toString()).put("goal", it.goal))
                }
            })
        }.toString()

        fun decode(json: String): StreakState = runCatching {
            val o = JSONObject(json)
            val goals = o.getJSONArray("goals")
            StreakState(
                startDate = if (o.isNull("start")) null else LocalDate.parse(o.getString("start")),
                brokenThrough = if (o.isNull("broken")) null else LocalDate.parse(o.getString("broken")),
                goalHistory = (0 until goals.length()).map {
                    val g = goals.getJSONObject(it)
                    GoalChange(LocalDate.parse(g.getString("from")), g.getInt("goal"))
                }
            )
        }.onFailure {
            Log.w("SettingsDataStore", "Failed to decode streak state, resetting", it)
        }.getOrDefault(StreakState())
    }
}
