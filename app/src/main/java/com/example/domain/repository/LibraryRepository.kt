package com.example.domain.repository

import com.example.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    val games: Flow<List<Game>>

    fun getGameById(id: String): Flow<Game?>
    suspend fun refreshLibrary(force: Boolean = false): Result<Unit>
    suspend fun clearCache()
}
