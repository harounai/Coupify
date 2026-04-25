package com.generativecity.wallet.viewmodel

import androidx.lifecycle.ViewModel
import com.generativecity.wallet.data.model.Business
import com.generativecity.wallet.data.model.CouponTemplate
import com.generativecity.wallet.data.remote.MockData
import com.generativecity.wallet.data.repository.OfferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ExploreFilterMode {
    COUPONS,
    ACTIVITY,
    NEAREST,
    PERSONALIZED
}

data class ExploreUiState(
    val businesses: List<Business> = MockData.businesses,
    val coupons: List<CouponTemplate> = MockData.coupons,
    val selectedCategory: String = "all",
    val maxDistanceKm: Double = 5.0,
    val filterMode: ExploreFilterMode = ExploreFilterMode.PERSONALIZED,
    val currentLocationName: String = "Munich City Center"
)

class ExploreViewModel(
    @Suppress("UNUSED_PARAMETER") private val offerRepository: OfferRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    fun setCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setDistance(distance: Double) {
        _uiState.update { it.copy(maxDistanceKm = distance) }
    }

    fun setFilterMode(mode: ExploreFilterMode) {
        _uiState.update { it.copy(filterMode = mode) }
    }
    
    fun setLocation(location: String) {
        _uiState.update { it.copy(currentLocationName = location) }
    }
}
