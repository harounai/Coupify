package com.generativecity.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.generativecity.wallet.data.remote.CouponInstanceDto
import com.generativecity.wallet.data.remote.HomeFeedDto
import com.generativecity.wallet.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val feed: HomeFeedDto? = null
)

class HomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun refresh(lat: Double? = null, lon: Double? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { homeRepository.fetchHome(lat = lat, lon = lon) }
                .onSuccess { feed -> _uiState.update { it.copy(isLoading = false, feed = feed) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load") } }
        }
    }

    fun claim(coupon: CouponInstanceDto) {
        viewModelScope.launch {
            val tplId = coupon.template.id
            val bizId = coupon.business.id
            runCatching { homeRepository.claim(templateId = tplId, businessId = bizId) }
                .onSuccess { claimed ->
                    // Optimistic UI update: show it immediately under "Claimed Rewards",
                    // then refresh in the background to reconcile.
                    _uiState.update { state ->
                        val existing = state.feed?.claimed_rewards_today ?: emptyList()
                        val merged = (listOf(claimed) + existing).distinctBy { it.id }
                        state.copy(feed = state.feed?.copy(claimed_rewards_today = merged))
                    }
                    refresh()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to claim") }
                }
        }
    }

    fun redeem(couponId: String) {
        viewModelScope.launch {
            runCatching { homeRepository.redeem(couponId) }
                .onSuccess {
                    refresh()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to expire coupon") }
                }
        }
    }
}

