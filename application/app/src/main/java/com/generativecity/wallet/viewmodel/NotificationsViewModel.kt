package com.generativecity.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.generativecity.wallet.data.local.OfferEntity
import com.generativecity.wallet.data.model.OfferStatus
import com.generativecity.wallet.data.repository.OfferRepository
import com.generativecity.wallet.data.repository.RewardRepository
import com.generativecity.wallet.domain.usecase.GeneratePersonalizedOfferUseCase
import com.generativecity.wallet.utils.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val notifications: List<OfferEntity> = emptyList(),
    val decisionSecondsRemaining: Map<String, Long> = emptyMap(),
    val paymentInProgressOfferId: String? = null,
    val doubleOrNothingCount: Int = 0,
    val timeExtensionCount: Int = 0
)

class NotificationsViewModel(
    private val offerRepository: OfferRepository,
    private val rewardRepository: RewardRepository,
    private val generatePersonalizedOfferUseCase: GeneratePersonalizedOfferUseCase,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val firstSeenEpochMillis = mutableMapOf<String, Long>()
    private var lastGeneratedAt = System.currentTimeMillis()

    fun observe(userId: Int) {
        viewModelScope.launch {
            rewardRepository.observeInventory(userId).collect { inv ->
                _uiState.update { it.copy(
                    doubleOrNothingCount = inv?.doubleOrNothingCount ?: 0,
                    timeExtensionCount = inv?.timeExtensionCount ?: 0
                ) }
            }
        }

        viewModelScope.launch {
            offerRepository.observeOffers(userId).collect { offers ->
                val tracked = offers.filter {
                    it.status == OfferStatus.ACTIVE ||
                        it.status == OfferStatus.NOTIFICATION_ACCEPTED ||
                        it.status == OfferStatus.INSTANT_PAID
                }

                tracked.forEach { offer ->
                    if (!firstSeenEpochMillis.containsKey(offer.id)) {
                        firstSeenEpochMillis[offer.id] = offer.createdEpochMillis
                    }
                }

                val existingIds = tracked.map { it.id }.toSet()
                firstSeenEpochMillis.keys.retainAll(existingIds)

                _uiState.update { state ->
                    state.copy(notifications = tracked)
                }
            }
        }

        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                
                // Decision logic
                val decisionMap = _uiState.value.notifications.associate { offer ->
                    val start = firstSeenEpochMillis[offer.id] ?: now
                    // 1 hour to decide
                    val remaining = ((start + 60 * 60 * 1000) - now) / 1000
                    offer.id to remaining.coerceAtLeast(0)
                }

                // If offer expired (1 hour passed), remove it or update status
                _uiState.value.notifications.forEach { offer ->
                    if (offer.status == OfferStatus.ACTIVE && (decisionMap[offer.id] ?: 0) <= 0) {
                        decline(offer.id)
                    }
                }

                // Periodic generation logic: every 5 minutes
                if ((now - lastGeneratedAt) >= 5 * 60 * 1000) {
                    val generated = generatePersonalizedOfferUseCase(userId)
                    offerRepository.persistGeneratedOffer(userId, generated)
                    
                    // Trigger Push Notification
                    notificationHelper.showCouponNotification(
                        businessName = generated.businessName,
                        title = generated.title,
                        discount = generated.discount,
                        imageUrl = generated.imageUrl
                    )

                    lastGeneratedAt = now
                }

                _uiState.update { it.copy(decisionSecondsRemaining = decisionMap) }
                
                delay(1_000)
            }
        }
    }

    fun canDecide(offerId: String): Boolean {
        return (_uiState.value.decisionSecondsRemaining[offerId] ?: 0) > 0
    }

    fun decline(offerId: String) {
        viewModelScope.launch {
            offerRepository.updateOfferStatus(offerId, OfferStatus.REDEEMED) // Effectively archive
        }
    }

    fun accept(offerId: String) {
        if (!canDecide(offerId)) return
        viewModelScope.launch {
            offerRepository.updateOfferStatus(offerId, OfferStatus.NOTIFICATION_ACCEPTED)
        }
    }

    fun instantPay(offerId: String) {
        if (!canDecide(offerId)) return
        viewModelScope.launch {
            _uiState.update { it.copy(paymentInProgressOfferId = offerId) }
            delay(1300)
            // Instant pay makes it better (e.g. +5% bonus discount)
            val offer = _uiState.value.notifications.find { it.id == offerId }
            if (offer != null) {
                offerRepository.updateOfferStatus(offerId, OfferStatus.INSTANT_PAID)
            }
            _uiState.update { it.copy(paymentInProgressOfferId = null) }
        }
    }

    fun applyDoubleOrNothing(userId: Int, offerId: String) {
        viewModelScope.launch {
            val inv = rewardRepository.getInventory(userId)
            if (inv != null && inv.doubleOrNothingCount > 0) {
                rewardRepository.updateInventory(inv.copy(doubleOrNothingCount = inv.doubleOrNothingCount - 1))
                val offer = _uiState.value.notifications.find { it.id == offerId }
                if (offer != null) {
                    // Double the discount
                    val updated = offer.copy(discountPercent = (offer.discountPercent * 2).coerceAtMost(100))
                    offerRepository.updateOffer(updated)
                }
            }
        }
    }

    fun applyTimeExtension(userId: Int, offerId: String) {
        viewModelScope.launch {
            val inv = rewardRepository.getInventory(userId)
            if (inv != null && inv.timeExtensionCount > 0) {
                rewardRepository.updateInventory(inv.copy(timeExtensionCount = inv.timeExtensionCount - 1))
                val offer = _uiState.value.notifications.find { it.id == offerId }
                if (offer != null) {
                    // Extend expiry by 1 day
                    val updated = offer.copy(expiryEpochMillis = offer.expiryEpochMillis + 24 * 60 * 60 * 1000)
                    offerRepository.updateOffer(updated)
                }
            }
        }
    }
}
