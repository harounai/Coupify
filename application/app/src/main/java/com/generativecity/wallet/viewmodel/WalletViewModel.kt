package com.generativecity.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.generativecity.wallet.data.local.OfferEntity
import com.generativecity.wallet.data.model.OfferStatus
import com.generativecity.wallet.data.repository.OfferRepository
import com.generativecity.wallet.domain.usecase.GeneratePersonalizedOfferUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WalletUiState(
    val activeOffers: List<OfferEntity> = emptyList(),
    val savedOffers: List<OfferEntity> = emptyList(),
    val redeemedOffers: List<OfferEntity> = emptyList(),
    val selectedOfferIdForQr: String? = null
)

class WalletViewModel(
    private val offerRepository: OfferRepository,
    private val generatePersonalizedOfferUseCase: GeneratePersonalizedOfferUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun observeOffers(userId: Int) {
        viewModelScope.launch {
            offerRepository.observeOffers(userId).collect { offers ->
                _uiState.update {
                    it.copy(
                        activeOffers = offers.filter { offer -> offer.status == OfferStatus.ACTIVE },
                        savedOffers = offers.filter { offer ->
                            offer.status == OfferStatus.SAVED ||
                                offer.status == OfferStatus.NOTIFICATION_ACCEPTED ||
                                offer.status == OfferStatus.INSTANT_PAID
                        },
                        redeemedOffers = offers.filter { offer -> offer.status == OfferStatus.REDEEMED }
                    )
                }
            }
        }
    }

    fun refreshGeneratedOffer(userId: Int) {
        viewModelScope.launch {
            val generated = generatePersonalizedOfferUseCase(userId)
            offerRepository.persistGeneratedOffer(userId, generated)
        }
    }

    fun saveOffer(offerId: String) {
        viewModelScope.launch {
            val alreadySaved = _uiState.value.savedOffers.any { it.id == offerId } || 
                               _uiState.value.redeemedOffers.any { it.id == offerId }
            
            if (alreadySaved) {
                _events.emit("already_in_wallet")
            } else {
                offerRepository.updateOfferStatus(offerId, OfferStatus.SAVED)
            }
        }
    }

    fun redeemOffer(offerId: String) {
        viewModelScope.launch {
            // We don't remove it from wallet, just update its status/time if needed
            // For demo, we can just keep it in SAVED or move to REDEEMED but keep it visible
            offerRepository.updateOfferStatus(offerId, OfferStatus.REDEEMED)
        }
    }

    fun openQrForOffer(offerId: String) {
        _uiState.update { it.copy(selectedOfferIdForQr = offerId) }
    }

    fun closeQr() {
        _uiState.update { it.copy(selectedOfferIdForQr = null) }
    }
}
