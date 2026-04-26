package com.generativecity.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.generativecity.wallet.data.remote.MerchantBusinessDto
import com.generativecity.wallet.data.remote.MerchantRuleDto
import com.generativecity.wallet.data.remote.MerchantRuleInDto
import com.generativecity.wallet.data.remote.MerchantStatsDto
import com.generativecity.wallet.data.repository.MerchantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MerchantDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val businesses: List<MerchantBusinessDto> = emptyList(),
    val selectedBusinessId: String? = null,
    val rules: MerchantRuleDto? = null,
    val stats: MerchantStatsDto? = null
)

class MerchantDashboardViewModel(
    private val merchantRepository: MerchantRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MerchantDashboardUiState())
    val uiState: StateFlow<MerchantDashboardUiState> = _uiState.asStateFlow()

    fun load(businessId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, selectedBusinessId = businessId) }
            runCatching {
                val businesses = merchantRepository.listBusinesses()
                val rules = merchantRepository.getRules(businessId)
                val stats = merchantRepository.getStats(businessId)
                Triple(businesses, rules, stats)
            }.onSuccess { (businesses, rules, stats) ->
                _uiState.update { it.copy(isLoading = false, businesses = businesses, rules = rules, stats = stats) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load merchant data") }
            }
        }
    }

    fun saveRules(maxDiscount: Int, minDiscount: Int, goal: String) {
        val businessId = _uiState.value.selectedBusinessId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                merchantRepository.saveRules(
                    businessId,
                    MerchantRuleInDto(
                        max_discount_percent = maxDiscount,
                        min_discount_percent = minDiscount,
                        quiet_hours_start = null,
                        quiet_hours_end = null,
                        goal = goal,
                        coupons_per_day = _uiState.value.rules?.coupons_per_day ?: 50,
                        coupons_total = _uiState.value.rules?.coupons_total ?: 1000,
                        products = _uiState.value.rules?.products ?: emptyList()
                    )
                )
            }.onSuccess { rules ->
                val stats = runCatching { merchantRepository.getStats(businessId) }.getOrNull()
                _uiState.update { it.copy(isLoading = false, rules = rules, stats = stats ?: it.stats) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to save rules") }
            }
        }
    }

    fun saveAdvancedRules(
        maxDiscount: Int,
        minDiscount: Int,
        goal: String,
        couponsPerDay: Int,
        couponsTotal: Int,
        products: List<String>
    ) {
        val businessId = _uiState.value.selectedBusinessId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                merchantRepository.saveRules(
                    businessId,
                    MerchantRuleInDto(
                        max_discount_percent = maxDiscount,
                        min_discount_percent = minDiscount,
                        quiet_hours_start = null,
                        quiet_hours_end = null,
                        goal = goal,
                        coupons_per_day = couponsPerDay,
                        coupons_total = couponsTotal,
                        products = products
                    )
                )
            }.onSuccess { rules ->
                val stats = runCatching { merchantRepository.getStats(businessId) }.getOrNull()
                _uiState.update { it.copy(isLoading = false, rules = rules, stats = stats ?: it.stats) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to save rules") }
            }
        }
    }

    fun simulateLowDemand() {
        val businessId = _uiState.value.selectedBusinessId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { merchantRepository.simulateLowDemand(businessId, minutes = 60, demandLevel = 20) }
                .onSuccess {
                    // refresh stats after signal change
                    val stats = runCatching { merchantRepository.getStats(businessId) }.getOrNull()
                    _uiState.update { it.copy(isLoading = false, stats = stats ?: it.stats) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to simulate demand") }
                }
        }
    }
}

