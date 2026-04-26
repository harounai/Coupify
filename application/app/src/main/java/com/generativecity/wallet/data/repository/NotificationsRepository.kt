package com.generativecity.wallet.data.repository

import com.generativecity.wallet.data.local.WalletDao
import com.generativecity.wallet.data.remote.BackendApiService
import com.generativecity.wallet.data.remote.NotificationInboxItemDto
import com.generativecity.wallet.data.remote.RegisterDeviceRequestDto

class NotificationsRepository(
    private val api: BackendApiService,
    private val walletDao: WalletDao
) {
    private suspend fun token(): String {
        val t = walletDao.getLatestUser()?.token ?: error("Missing session token")
        return "Bearer $t"
    }

    suspend fun inbox(): List<NotificationInboxItemDto> = api.getNotificationInbox(token())

    suspend fun accept(notificationId: String) {
        api.acceptNotification(token(), notificationId)
    }

    suspend fun decline(notificationId: String) {
        api.declineNotification(token(), notificationId)
    }

    suspend fun registerDeviceToken(fcmToken: String) {
        api.registerDevice(token(), RegisterDeviceRequestDto(fcm_token = fcmToken))
    }
}

