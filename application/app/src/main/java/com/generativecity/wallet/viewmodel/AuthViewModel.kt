package com.generativecity.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.generativecity.wallet.data.local.UserEntity
import com.generativecity.wallet.data.remote.UserPreferencesDto
import com.generativecity.wallet.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: UserEntity? = null,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val onboardingCompleted: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isLoggedIn = user != null,
                        onboardingCompleted = user?.hasCompletedOnboarding ?: false
                    )
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.login(email, password)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
            }
            result.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Login failed") }
            }
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.register(email, password, displayName)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
            }
            result.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Registration failed") }
            }
        }
    }

    fun completeOnboarding(preferences: UserPreferencesDto) {
        val user = _uiState.value.currentUser ?: return
        val token = user.token ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.updatePreferences(token, preferences)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, onboardingCompleted = true) }
            }
            result.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to save preferences") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
