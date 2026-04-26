package com.generativecity.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.generativecity.wallet.data.repository.NotificationsRepository
import com.generativecity.wallet.utils.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val lastPollAtEpochMillis: Long? = null
)

class NotificationsViewModel(
    private val notificationsRepository: NotificationsRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val seenNotificationIds = mutableSetOf<String>()

    fun observe(@Suppress("UNUSED_PARAMETER") userId: String) {
        viewModelScope.launch {
            while (true) {
                runCatching { notificationsRepository.inbox() }
                    .onSuccess { inbox ->
                        inbox
                            .filter { it.status == "pending" }
                            .filter { !seenNotificationIds.contains(it.id) }
                            .forEach { item ->
                                seenNotificationIds.add(item.id)
                                notificationHelper.showCouponNotification(
                                    notificationId = item.id,
                                    businessName = item.business_name.ifBlank { "New offer" },
                                    title = item.title,
                                    discount = item.discount_percent,
                                    imageUrl = item.image_url
                                )
                            }
                        _uiState.update { it.copy(lastPollAtEpochMillis = System.currentTimeMillis()) }
                    }
                delay(15_000)
            }
        }
    }

    fun decline(notificationId: String) {
        viewModelScope.launch {
            notificationsRepository.decline(notificationId)
        }
    }

    fun accept(notificationId: String) {
        viewModelScope.launch {
            notificationsRepository.accept(notificationId)
        }
    }

    fun instantPay(notificationId: String) {
        // Use the same accept endpoint for now; the "Pay Now" flow is handled elsewhere in UI.
        accept(notificationId)
    }

    // Legacy hooks still referenced by HomeScreen. No-ops for now (will be reintroduced with
    // real inventory/rewards mechanics once backend-driven notifications are fully wired).
    fun applyDoubleOrNothing(userId: String, offerId: String) = Unit

    fun applyTimeExtension(userId: String, offerId: String) = Unit
}
