package com.generativecity.wallet.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.generativecity.wallet.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "coupon_notifications"
        private const val CHANNEL_NAME = "Coupon Offers"
        private const val CHANNEL_DESCRIPTION = "Notifications for new available coupons"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = 0xFFF97316.toInt()
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showCouponNotification(businessName: String, title: String, discount: Int, imageUrl: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val bitmap = downloadImage(imageUrl)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(intent)
                getPendingIntent(
                    businessName.hashCode(),
                    PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
                )
            }
            
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("New Offer: $businessName")
                .setContentText("$title - $discount% OFF!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setColor(0xFFF97316.toInt())
                .setContentIntent(pendingIntent)
                .apply {
                    if (bitmap != null) {
                        setLargeIcon(bitmap)
                        setStyle(NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null as Bitmap?))
                    }
                }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(businessName.hashCode(), builder.build())
        }
    }

    private suspend fun downloadImage(url: String): Bitmap? {
        if (url.isBlank()) return null
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .build()
        
        val result = loader.execute(request)
        return if (result is SuccessResult) {
            (result.drawable as BitmapDrawable).bitmap
        } else {
            null
        }
    }
}
