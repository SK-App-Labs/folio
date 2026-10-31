// File: app/src/main/java/com/folioledger/app/ui/settings/sheets/FolioLocaleManager.kt
package com.folioledger.app.ui.settings.sheets

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Pure Android platform-level Locale Manager for Folio.
 * Leverages native Android 13+ (API 33+) LocaleManager for OS per-app language settings,
 * with configuration-context wrapping fallback for pre-Android 13 devices,
 * avoiding any AppCompatActivity or AppCompat theme requirements.
 */
object FolioLocaleManager {
    private const val PREFS_NAME = "folio_locale_prefs"
    private const val KEY_LANGUAGE_TAG = "selected_language_tag"

    fun setAppLanguage(context: Context, languageTag: String) {
        // Persist to local preferences
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE_TAG, languageTag)
            .apply()

        // On Android 13+ (API 33+), notify the Android OS LocaleManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            if (localeManager != null) {
                localeManager.applicationLocales = if (languageTag.isBlank()) {
                    LocaleList.getEmptyLocaleList()
                } else {
                    LocaleList.forLanguageTags(languageTag)
                }
            }
        }

        // Recreate the hosting Activity so all Compose screens reload with the new locale
        findActivity(context)?.recreate()
    }

    fun getCurrentLanguageTag(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            val locales = localeManager?.applicationLocales
            if (locales != null && !locales.isEmpty) {
                return locales[0]?.toLanguageTag() ?: ""
            }
        }
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE_TAG, "") ?: ""
    }

    fun wrapContextIfNeeded(context: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context
        }
        val tag = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE_TAG, "") ?: ""
        if (tag.isBlank()) return context

        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    fun findActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}
