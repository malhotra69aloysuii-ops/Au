package com.example.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.TokenStore
import com.example.domain.model.EpicTokens
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object InWebView : AuthUiState
    data object ExchangingCode : AuthUiState
    data class Success(val tokens: EpicTokens) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore,
    private val credentialsUpdater: com.example.data.repository.CredentialsUpdater
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentTokens = authRepository.currentTokens

    init {
        // Freshly parse maintained client credentials once on initialization
        viewModelScope.launch {
            credentialsUpdater.refreshCredentialsFromGitHub()
        }
    }

    fun getClientId(): String = tokenStore.getCustomClientId()

    fun openWebView() {
        viewModelScope.launch {
            credentialsUpdater.refreshCredentialsFromGitHub()
        }
        _uiState.value = AuthUiState.InWebView
    }

    fun closeWebView() {
        _uiState.value = AuthUiState.Idle
    }

    fun handleAuthCode(code: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.ExchangingCode
            // Ensure credentials are fully refreshed before exchanging code
            credentialsUpdater.refreshCredentialsFromGitHub()
            authRepository.exchangeCode(code)
                .onSuccess { tokens ->
                    _uiState.value = AuthUiState.Success(tokens)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(
                        error.message ?: "Authentication failed. Please verify credentials."
                    )
                }
        }
    }

    fun handleAuthError(error: String) {
        _uiState.value = AuthUiState.Error("Sign-in cancelled or failed: $error")
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            tokenStore: TokenStore,
            credentialsUpdater: com.example.data.repository.CredentialsUpdater
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authRepository, tokenStore, credentialsUpdater) as T
            }
        }
    }
}
