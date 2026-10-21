package com.example.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.TokenStore
import com.example.data.util.NetworkMonitor
import com.example.domain.model.Game
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.LibraryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibrarySortOption {
    ALPHABETICAL_ASC,
    ALPHABETICAL_DESC,
    RECENTLY_ADDED
}

class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(LibrarySortOption.ALPHABETICAL_ASC)
    val sortOption: StateFlow<LibrarySortOption> = _sortOption.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isOffline = MutableStateFlow(!networkMonitor.isCurrentlyOnline())
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _showOfflineBanner = MutableStateFlow(false)
    val showOfflineBanner: StateFlow<Boolean> = _showOfflineBanner.asStateFlow()

    private var offlineDismissJob: Job? = null

    val displayName: String
        get() = tokenStore.getDisplayName()

    // Efficiently filtered and sorted games flow directly backed by Room database
    val games: StateFlow<List<Game>> = combine(
        libraryRepository.games,
        _searchQuery,
        _sortOption
    ) { allGames, rawQuery, sort ->
        val query = rawQuery.trim()
        val filtered = if (query.isEmpty()) {
            allGames
        } else {
            allGames.filter { game ->
                game.name.contains(query, ignoreCase = true)
            }
        }

        when (sort) {
            LibrarySortOption.ALPHABETICAL_ASC -> filtered.sortedBy { it.name.lowercase() }
            LibrarySortOption.ALPHABETICAL_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            LibrarySortOption.RECENTLY_ADDED -> filtered.sortedByDescending { it.ownedSince }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Observe network state changes
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _isOffline.value = !online
                if (!online) {
                    // Device went offline: show offline banner for 5 seconds
                    triggerOfflineBanner()
                } else {
                    // Device is online: update cache removing old items
                    _showOfflineBanner.value = false
                    offlineDismissJob?.cancel()
                    refreshLibrary(force = true)
                }
            }
        }

        if (!networkMonitor.isCurrentlyOnline()) {
            triggerOfflineBanner()
        }
    }

    private fun triggerOfflineBanner() {
        offlineDismissJob?.cancel()
        _showOfflineBanner.value = true
        offlineDismissJob = viewModelScope.launch {
            delay(5000)
            _showOfflineBanner.value = false
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSortOptionChanged(option: LibrarySortOption) {
        _sortOption.value = option
    }

    fun refreshLibrary(force: Boolean = true) {
        viewModelScope.launch {
            if (!networkMonitor.isCurrentlyOnline()) {
                triggerOfflineBanner()
                return@launch
            }
            _isRefreshing.value = true
            _errorMessage.value = null
            libraryRepository.refreshLibrary(force = force)
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to sync library."
                }
            _isRefreshing.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        fun provideFactory(
            libraryRepository: LibraryRepository,
            authRepository: AuthRepository,
            tokenStore: TokenStore,
            networkMonitor: NetworkMonitor
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LibraryViewModel(libraryRepository, authRepository, tokenStore, networkMonitor) as T
            }
        }
    }
}
