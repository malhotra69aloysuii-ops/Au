package com.example.data.repository

import android.util.Log
import com.example.data.local.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class CredentialsUpdater(
    private val okHttpClient: OkHttpClient,
    private val tokenStore: TokenStore
) {
    private var hasParsedThisSession = false

    suspend fun refreshCredentialsFromGitHub(): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        if (hasParsedThisSession) {
            return@withContext Result.success(
                Pair(tokenStore.getCustomClientId(), tokenStore.getCustomClientSecret())
            )
        }

        val githubSources = listOf(
            "https://raw.githubusercontent.com/Jaren8r/EpicClients/master/README.md",
            "https://raw.githubusercontent.com/MixV2/EpicResearch/master/docs/auth/auth_clients.md"
        )

        for (url in githubSources) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful && response.body != null) {
                        val content = response.body!!.string()
                        val credentials = parseLauncherAppClient2(content)
                        if (credentials != null) {
                            val (clientId, clientSecret) = credentials
                            tokenStore.updateCredentialsFromRemote(clientId, clientSecret)
                            hasParsedThisSession = true
                            Log.d(TAG, "Successfully parsed and updated credentials from GitHub ($url)")
                            return@withContext Result.success(credentials)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed fetching credentials from $url: ${e.message}")
            }
        }

        // Fallback to verified local defaults
        val fallbackId = TokenStore.DEFAULT_CLIENT_ID
        val fallbackSecret = TokenStore.DEFAULT_CLIENT_SECRET
        tokenStore.updateCredentialsFromRemote(fallbackId, fallbackSecret)
        hasParsedThisSession = true
        Result.success(Pair(fallbackId, fallbackSecret))
    }

    private fun parseLauncherAppClient2(content: String): Pair<String, String>? {
        for (line in content.lineSequence()) {
            if (line.contains("launcherAppClient2", ignoreCase = true)) {
                // Table rows are pipe-delimited: | [launcherAppClient2...] | `ID` | `SECRET` | ...
                val columns = line.split("|").map { it.trim().replace("`", "") }
                // Look for 32-char hex client ID and secret
                val hexCandidate1 = columns.getOrNull(2)?.trim()
                val hexCandidate2 = columns.getOrNull(3)?.trim()

                if (!hexCandidate1.isNullOrBlank() && !hexCandidate2.isNullOrBlank() &&
                    hexCandidate1.length == 32 && hexCandidate2.length == 32
                ) {
                    return Pair(hexCandidate1, hexCandidate2)
                }

                // Fallback regex in the line
                val regex = Regex("""[a-f0-9]{32}""", RegexOption.IGNORE_CASE)
                val matches = regex.findAll(line).map { it.value }.toList()
                // In Jaren8r README, launcherAppClient2 has markdown link containing ID in url, then ID in col 2, then secret in col 3
                // Look for secret that is distinct from ID
                if (matches.size >= 2) {
                    val id = matches[0]
                    val secret = matches.firstOrNull { it != id && it != "daafbccc737745039d07ba13ae6b6500" }
                        ?: matches.getOrNull(matches.size - 1)
                    if (secret != null && secret.length == 32) {
                        return Pair(id, secret)
                    }
                }
            }
        }
        return null
    }

    companion object {
        private const val TAG = "CredentialsUpdater"
    }
}
