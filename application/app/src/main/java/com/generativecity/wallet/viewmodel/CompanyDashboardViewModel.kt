package com.generativecity.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.generativecity.wallet.data.local.OfferEntity
import com.generativecity.wallet.data.local.UserEntity
import com.generativecity.wallet.data.repository.OfferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompanyDashboardUiState(
    val activeOffers: List<OfferEntity> = emptyList(),
    val lastSuggestionMessage: String = "No AI suggestion generated yet"
)

class CompanyDashboardViewModel(
    private val offerRepository: OfferRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompanyDashboardUiState())
    val uiState: StateFlow<CompanyDashboardUiState> = _uiState.asStateFlow()

    fun observeOffers(userId: Int) {
        viewModelScope.launch {
            offerRepository.observeOffers(userId).collect { offers ->
                _uiState.update { it.copy(activeOffers = offers) }
            }
        }
    }

    fun generateSuggestion(userId: Int, user: UserEntity) {
        viewModelScope.launch {
            offerRepository.createOfferFromCompany(userId, user)
            _uiState.update { it.copy(lastSuggestionMessage = "AI suggestion created for ${user.companyName}") }
        }
    }
}
