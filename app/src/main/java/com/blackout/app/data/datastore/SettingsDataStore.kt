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

/**
 * Everything MainActivity needs before it can draw the first frame, read atomically from a
 * single snapshot. distinctUntilChanged() below means unrelated writes (e.g. streak updates)
 * don't re-emit this and don't recompose the root.
 */
data class UiSettings(
    val seedColor: Int,
    val themeMode: String,       // "light" | "dark" | "system"
    val dynamicColor: Boolean,
    val paletteStyle: String,    // PaletteStyle.name, parsed in the UI layer
    val language: String,

    )

/** Per-feature switches. Each defaults to the style's preset until the user changes it. */
data class FeatureToggles(
    val streak: Boolean,
) {
}

// Replaces ThemeSettingsProvider, LanguageProvider, GenderSettingsProvider,
// UnitSettingsProvider, and the seed-color part of your Appainter setup.
// These are key-value prefs, not relational data, so DataStore is the right
// fit rather than another Room table — same role Hive's misc boxes played.
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // 0xFFC9A24B = the warm antique-gold default from app_theme.dart
        const val DEFAULT_SEED = 0xFFC9A24B.toInt()
        const val DEFAULT_PALETTE_STYLE = "TonalSpot"
        const val LANGUAGE_SYSTEM = "system"
    }

    private object Keys {
        val SEED_COLOR = intPreferencesKey("seed_color_argb")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "light" | "dark" | "system"
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val PALETTE_STYLE = stringPreferencesKey("palette_style") // PaletteStyle.name
        val UNIT_SYSTEM = stringPreferencesKey("unit_system") // "metric" | "imperial"
        val GODS = stringPreferencesKey("gods_option")
        val LANGUAGE = stringPreferencesKey("language_code")
        val GENDER = stringPreferencesKey("gender") // LEGACY: read-only, only used to migrate
        val SEX = stringPreferencesKey("sex")       // Sex.name
        val STYLE = stringPreferencesKey("style")   // Style.name
        val FEATURE_STREAK = booleanPreferencesKey("feature_streak")
        val FEATURE_LABORS = booleanPreferencesKey("feature_labors")
        val FEATURE_IDEALS = booleanPreferencesKey("feature_ideals")
        val FEATURE_GOALS = booleanPreferencesKey("feature_goals")
        val FEATURE_GODS = booleanPreferencesKey("feature_gods")
        val WEEKLY_GOAL = intPreferencesKey("weekly_goal")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

        val STREAK_STATE = stringPreferencesKey("streak_state")
        val FAVORITE_QUOTES = stringSetPreferencesKey("favorite_quote_ids")
    }

    // One shared upstream: an unreadable/corrupt file falls back to defaults instead of
    // crashing every collector.
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

    private fun Preferences.effectiveSeed(): Int =
        this[Keys.SEED_COLOR] ?: 0xFFC9A24B.toInt()




    val streakEnabled: Flow<Boolean> = prefs.map { it.streakOn() }.distinctUntilChanged()


    /** All three together, for code that gates several features at once. */
    val features: Flow<FeatureToggles> = prefs.map {
        FeatureToggles(streak = it.streakOn())
    }.distinctUntilChanged()

    val seedColor: Flow<Int> = prefs.map { it.effectiveSeed() }
    val themeMode: Flow<String> = prefs.map { it[Keys.THEME_MODE] ?: "system" }
    val dynamicColor: Flow<Boolean> = prefs.map { it[Keys.DYNAMIC_COLOR] ?: false }
    val paletteStyle: Flow<String> = prefs.map { it[Keys.PALETTE_STYLE] ?: DEFAULT_PALETTE_STYLE }
    val language: Flow<String> = prefs.map { it[Keys.LANGUAGE] ?: LANGUAGE_SYSTEM }
    /**
     * Bridge for code that still reads the old string. Migrate those call sites to sex/style
     * (e.g. `gender != "female_soft"` -> `style == Style.HARD`) and then delete this.
     * Note MALE+SOFT has no legacy equivalent and maps to "male".
     */

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

    /** Back to "follow the persona's default colour". */
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