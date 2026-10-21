package com.example.data.repository

import android.content.Context
import android.util.Base64
import android.util.Log
import android.webkit.CookieManager
import coil.ImageLoader
import com.example.data.api.EpicAuthApi
import com.example.data.local.GameDao
import com.example.data.local.TokenStore
import com.example.domain.model.EpicTokens
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val context: Context,
    private val authApi: EpicAuthApi,
    private val tokenStore: TokenStore,
    private val gameDao: GameDao,
    private val imageLoader: ImageLoader
) : AuthRepository {

    private val _currentTokens = MutableStateFlow(tokenStore.getTokens())
    override val currentTokens: StateFlow<EpicTokens?> = _currentTokens.asStateFlow()

    override fun isLoggedIn(): Boolean = tokenStore.isLoggedIn()

    override fun getValidAccessToken(): String? {
        val tokens = _currentTokens.value ?: return null
        return tokens.accessToken
    }

    private fun getBasicAuthHeader(): String {
        val clientId = tokenStore.getCustomClientId()
        val clientSecret = tokenStore.getCustomClientSecret()
        val raw = "$clientId:$clientSecret"
        val encoded = Base64.encodeToString(raw.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "Basic $encoded"
    }

    override suspend fun exchangeCode(code: String): Result<EpicTokens> = withContext(Dispatchers.IO) {
        runCatching {
            val basicAuth = getBasicAuthHeader()
            val response = authApi.exchangeCode(
                basicAuth = basicAuth,
                code = code
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val tokens = EpicTokens(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    expiresIn = body.expiresIn,
                    accountId = body.accountId,
                    displayName = body.displayName ?: "Epic Gamer",
                    fetchedAt = System.currentTimeMillis()
                )
                tokenStore.saveTokens(tokens)
                _currentTokens.value = tokens
                tokens
            } else {
                val err = response.errorBody()?.string() ?: "Failed to exchange code: HTTP ${response.code()}"
                Log.e(TAG, "Code exchange error: $err")
                throw Exception("Authentication failed: $err")
            }
        }
    }

    override suspend fun refreshToken(): Result<EpicTokens> = withContext(Dispatchers.IO) {
        val current = _currentTokens.value ?: return@withContext Result.failure(Exception("No active tokens"))

        runCatching {
            val basicAuth = getBasicAuthHeader()
            val response = authApi.refreshToken(
                basicAuth = basicAuth,
                refreshToken = current.refreshToken
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val newTokens = EpicTokens(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    expiresIn = body.expiresIn,
                    accountId = body.accountId.ifEmpty { current.accountId },
                    displayName = body.displayName ?: current.displayName,
                    fetchedAt = System.currentTimeMillis()
                )
                tokenStore.saveTokens(newTokens)
                _currentTokens.value = newTokens
                newTokens
            } else {
                Log.w(TAG, "Refresh token failed: ${response.code()}")
                // Clear on 400/401 per specification
                if (response.code() in 400..401) {
                    tokenStore.clear()
                    _currentTokens.value = null
                }
                throw Exception("Session expired or refresh failed: HTTP ${response.code()}")
            }
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        val current = _currentTokens.value

        // Step 1: Revoke server-side session (runCatching so offline logout still succeeds locally)
        if (current != null) {
            runCatching {
                authApi.killSession(
                    bearerAuth = "bearer ${current.accessToken}",
                    accessToken = current.accessToken
                )
            }.onFailure { e ->
                Log.w(TAG, "Server logout revocation failed: ${e.message}")
            }
        }

        // Step 2: Clear WebView cookies on Main thread
        withContext(Dispatchers.Main) {
            runCatching {
                val cookieManager = CookieManager.getInstance()
                cookieManager.removeAllCookies(null)
                cookieManager.flush()
            }.onFailure { e ->
                Log.w(TAG, "Cookie cleanup failed: ${e.message}")
            }
        }

        // Step 3: Wipe encrypted token store
        tokenStore.clear()
        _currentTokens.value = null

        // Step 4: Wipe library database
        runCatching {
            gameDao.clearAll()
        }.onFailure { e ->
            Log.w(TAG, "GameDao clear failed: ${e.message}")
        }

        // Step 5: Wipe image caches
        runCatching {
            imageLoader.memoryCache?.clear()
            imageLoader.diskCache?.clear()
        }.onFailure { e ->
            Log.w(TAG, "ImageLoader cache clear failed: ${e.message}")
        }

        Result.success(Unit)
    }

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }
}
