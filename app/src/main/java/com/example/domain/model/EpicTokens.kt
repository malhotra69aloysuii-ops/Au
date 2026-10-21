package com.example.domain.model

data class EpicTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val accountId: String,
    val displayName: String,
    val fetchedAt: Long = System.currentTimeMillis()
) {
    /**
     * Checks whether access token is expired with a 60-second buffer
     */
    fun isExpired(): Boolean {
        val expiryTime = fetchedAt + (expiresIn * 1000) - 60000
        return System.currentTimeMillis() >= expiryTime
    }
}
