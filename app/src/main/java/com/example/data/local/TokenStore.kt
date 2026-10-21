package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.domain.model.EpicTokens

class TokenStore(context: Context) {

    private val prefs: SharedPreferences = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "epic_secure_tokens",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrElse { error ->
        Log.w(TAG, "EncryptedSharedPreferences fallback to standard prefs: ${error.message}")
        context.getSharedPreferences("epic_secure_tokens_fallback", Context.MODE_PRIVATE)
    }

    fun saveTokens(tokens: EpicTokens) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
            .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            .putLong(KEY_EXPIRES_IN, tokens.expiresIn)
            .putString(KEY_ACCOUNT_ID, tokens.accountId)
            .putString(KEY_DISPLAY_NAME, tokens.displayName)
            .putLong(KEY_FETCHED_AT, tokens.fetchedAt)
            .apply()
    }

    fun getTokens(): EpicTokens? {
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null) ?: return null
        val expiresIn = prefs.getLong(KEY_EXPIRES_IN, 0L)
        val accountId = prefs.getString(KEY_ACCOUNT_ID, "") ?: ""
        val displayName = prefs.getString(KEY_DISPLAY_NAME, "Epic Gamer") ?: "Epic Gamer"
        val fetchedAt = prefs.getLong(KEY_FETCHED_AT, 0L)

        return EpicTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = expiresIn,
            accountId = accountId,
            displayName = displayName,
            fetchedAt = fetchedAt
        )
    }

    fun isLoggedIn(): Boolean {
        return getTokens() != null
    }

    fun getDisplayName(): String {
        return prefs.getString(KEY_DISPLAY_NAME, "Epic Gamer") ?: "Epic Gamer"
    }

    fun getAccountId(): String {
        return prefs.getString(KEY_ACCOUNT_ID, "") ?: ""
    }

    fun getCustomClientId(): String {
        return prefs.getString(KEY_CUSTOM_CLIENT_ID, DEFAULT_CLIENT_ID) ?: DEFAULT_CLIENT_ID
    }

    fun setCustomClientId(clientId: String) {
        prefs.edit().putString(KEY_CUSTOM_CLIENT_ID, clientId.trim()).apply()
    }

    fun getCustomClientSecret(): String {
        val stored = prefs.getString(KEY_CUSTOM_CLIENT_SECRET, DEFAULT_CLIENT_SECRET) ?: DEFAULT_CLIENT_SECRET
        if (stored.isBlank() || stored == "daafbccc737745039d07ba13ae6b6500" || stored.length != 32) {
            setCustomClientSecret(DEFAULT_CLIENT_SECRET)
            return DEFAULT_CLIENT_SECRET
        }
        return stored
    }

    fun setCustomClientSecret(secret: String) {
        val clean = secret.trim()
        val toSave = if (clean.isBlank() || clean == "daafbccc737745039d07ba13ae6b6500") DEFAULT_CLIENT_SECRET else clean
        prefs.edit().putString(KEY_CUSTOM_CLIENT_SECRET, toSave).apply()
    }

    fun updateCredentialsFromRemote(clientId: String, clientSecret: String) {
        val cleanId = clientId.trim()
        val cleanSecret = clientSecret.trim()
        if (cleanId.isNotBlank() && cleanSecret.isNotBlank() && cleanSecret != "daafbccc737745039d07ba13ae6b6500") {
            prefs.edit()
                .putString(KEY_CUSTOM_CLIENT_ID, cleanId)
                .putString(KEY_CUSTOM_CLIENT_SECRET, cleanSecret)
                .apply()
            Log.d(TAG, "Successfully updated client credentials from remote: id=$cleanId")
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val TAG = "TokenStore"
        // Community launcher credentials for personal/unofficial client use
        const val DEFAULT_CLIENT_ID = "34a02cf8f4414e29b15921876da36f9a"
        const val DEFAULT_CLIENT_SECRET = "daafbccc737745039dffe53d94fc76cf"

        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_IN = "expires_in"
        private const val KEY_ACCOUNT_ID = "account_id"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_FETCHED_AT = "fetched_at"
        private const val KEY_CUSTOM_CLIENT_ID = "custom_client_id"
        private const val KEY_CUSTOM_CLIENT_SECRET = "custom_client_secret"
    }
}
