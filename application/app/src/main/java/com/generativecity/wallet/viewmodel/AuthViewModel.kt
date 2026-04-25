package com.generativecity.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.generativecity.wallet.data.local.UserEntity
import com.generativecity.wallet.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: UserEntity? = null,
    val isLoggedIn: Boolean = false
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
                        isLoggedIn = user != null
                    )
                }
            }
        }
    }

    fun loginUser(username: String, interests: List<String>, explorationPreference: Int) {
        viewModelScope.launch {
            authRepository.loginUser(username, interests, explorationPreference)
        }
    }

    fun loginCompany(username: String, businessName: String, category: String, maxDiscountPercent: Int) {
        viewModelScope.launch {
            authRepository.loginCompany(username, businessName, category, maxDiscountPercent)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
