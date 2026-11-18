package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "abisor_settings")

enum class ModelTier(val id: String, val modelName: String) {
    FAST("fast", "gemini-3.5-flash-lite"),
    SMART("smart", "gemini-3.8-flash")
}

data class AppSettings(
    val language: AppLanguage = AppLanguage.BENGALI,
    val isDarkTheme: Boolean = false,
    val autoReadReplies: Boolean = false,
    val modelTier: ModelTier = ModelTier.FAST
)

class AppSettingsRepository(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val AUTO_READ = booleanPreferencesKey("auto_read_replies")
        val MODEL_TIER = stringPreferencesKey("model_tier")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val langCode = prefs[Keys.LANGUAGE] ?: AppLanguage.BENGALI.code
        val lang = AppLanguage.entries.firstOrNull { it.code == langCode } ?: AppLanguage.BENGALI
        val tierId = prefs[Keys.MODEL_TIER] ?: ModelTier.FAST.id
        val tier = ModelTier.entries.firstOrNull { it.id == tierId } ?: ModelTier.FAST
        AppSettings(
            language = lang,
            isDarkTheme = prefs[Keys.DARK_THEME] ?: false,
            autoReadReplies = prefs[Keys.AUTO_READ] ?: false,
            modelTier = tier
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setAutoReadReplies(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_READ] = enabled }
    }

    suspend fun setModelTier(tier: ModelTier) {
        context.dataStore.edit { it[Keys.MODEL_TIER] = tier.id }
    }
}
