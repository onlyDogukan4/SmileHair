package com.smilehair.selfcapture

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log

object ApiConfigManager {

    private const val PREFS_NAME = "api_config"
    private const val KEY_BASE_URL = "base_url"
    private const val DEFAULT_BASE_URL = "https://unfreakish-hottish-selene.ngrok-free.dev/"
    private const val TAG = "ApiConfigManager"

    private lateinit var preferences: SharedPreferences
    private val listeners = mutableListOf<() -> Unit>()

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = preferences.getString(KEY_BASE_URL, null)
        if (stored.isNullOrBlank()) {
            Log.d(TAG, "Base URL boş, varsayılan ngrok adresi kullanılıyor.")
            preferences.edit().putString(KEY_BASE_URL, DEFAULT_BASE_URL).apply()
        } else if (isLegacyLocalHost(stored)) {
            Log.d(TAG, "Legacy yerel IP tespit edildi ($stored). Ngrok adresine geçiriliyor.")
            preferences.edit().putString(KEY_BASE_URL, DEFAULT_BASE_URL).apply()
        }
    }

    fun getBaseUrl(): String {
        ensureInitialized()
        return preferences.getString(KEY_BASE_URL, DEFAULT_BASE_URL)!!
    }

    fun setBaseUrl(rawUrl: String): Boolean {
        ensureInitialized()
        val normalized = normalizeBaseUrl(rawUrl)
        if (normalized == null) return false
        if (normalized != getBaseUrl()) {
            preferences.edit().putString(KEY_BASE_URL, normalized).apply()
            notifyListeners()
        }
        return true
    }

    private fun normalizeBaseUrl(input: String): String? {
        val sanitized = input.trim()
        if (sanitized.isEmpty()) return null
        val uri = try {
            Uri.parse(sanitized)
        } catch (e: Exception) {
            return null
        }
        val scheme = uri.scheme ?: return null
        if (scheme != "http" && scheme != "https") return null
        var normalized = sanitized
        if (!normalized.endsWith("/")) {
            normalized += "/"
        }
        return normalized
    }

    private fun isLegacyLocalHost(url: String): Boolean {
        return try {
            val host = Uri.parse(url).host?.lowercase() ?: return false
            when {
                host == "localhost" -> true
                host.startsWith("10.") -> true
                host.startsWith("127.") -> true
                host.startsWith("192.168.") -> true
                host.startsWith("172.") -> {
                    val second = host.split(".").getOrNull(1)?.toIntOrNull()
                    second != null && second in 16..31
                }
                else -> false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.invoke() }
    }

    private fun ensureInitialized() {
        check(::preferences.isInitialized) { "ApiConfigManager.init must be called before use." }
    }
}

