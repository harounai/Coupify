package com.generativecity.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.generativecity.wallet.data.local.RewardInventoryEntity
import com.generativecity.wallet.data.repository.RewardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val inventory: RewardInventoryEntity? = null
)

class ProfileViewModel(
    private val rewardRepository: RewardRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun observeInventory(userId: String) {
        viewModelScope.launch {
            rewardRepository.markDailyLogin(userId)
            rewardRepository.observeInventory(userId).collect { inventory ->
                _uiState.update { it.copy(inventory = inventory) }
            }
        }
    }
}
