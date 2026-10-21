package com.example.domain.repository

import com.example.domain.model.EpicTokens
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentTokens: StateFlow<EpicTokens?>

    fun isLoggedIn(): Boolean
    fun getValidAccessToken(): String?

    suspend fun exchangeCode(code: String): Result<EpicTokens>
    suspend fun refreshToken(): Result<EpicTokens>
    suspend fun logout(): Result<Unit>
}
