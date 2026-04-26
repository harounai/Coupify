package com.generativecity.wallet.services

import com.generativecity.wallet.data.repository.AppContainer
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CitySparkFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        if (token.isBlank()) return
        if (runCatching { FirebaseApp.getApps(this).isNotEmpty() }.getOrDefault(false).not()) return
        val appContainer = AppContainer(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { appContainer.notificationsRepository.registerDeviceToken(token) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val notificationId = data["notification_id"] ?: "fcm_${System.currentTimeMillis()}"
        val businessName = data["business_name"] ?: "New offer"
        val title = message.notification?.title ?: data["title"] ?: "New offer"
        val imageUrl = data["image_url"] ?: ""
        val discount = data["discount_percent"]?.toIntOrNull() ?: 0

        val appContainer = AppContainer(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            appContainer.notificationHelper.showCouponNotification(
                notificationId = notificationId,
                businessName = businessName,
                title = title,
                discount = discount,
                imageUrl = imageUrl,
            )
        }
    }
}
