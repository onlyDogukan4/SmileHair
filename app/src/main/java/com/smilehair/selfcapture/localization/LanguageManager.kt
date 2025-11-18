package com.smilehair.selfcapture.localization

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(val code: String, val label: String) {
    TR("tr", "TR"),
    EN("en", "EN");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return values().firstOrNull { it.code == code } ?: TR
        }
    }
}

data class LocalizedText(val tr: String, val en: String) {
    fun get(): String {
        return if (LanguageManager.currentLanguage == AppLanguage.EN) en else tr
    }
}

object LanguageManager {

    private const val PREFS_NAME = "smilehair_language_settings"
    private const val KEY_LANGUAGE = "app_language"

    private var initialized = false

    var currentLanguage: AppLanguage = AppLanguage.TR
        private set

    fun init(context: Context) {
        if (initialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANGUAGE, AppLanguage.TR.code)
        currentLanguage = AppLanguage.fromCode(saved)
        updateAppLocales(currentLanguage)
        initialized = true
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        if (language == currentLanguage) return
        currentLanguage = language
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.code)
            .apply()
        updateAppLocales(language)
    }

    private fun updateAppLocales(language: AppLanguage) {
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}

