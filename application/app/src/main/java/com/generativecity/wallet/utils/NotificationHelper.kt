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

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "coupon_notifications"
        private const val CHANNEL_NAME = "Coupon Offers"
        private const val CHANNEL_DESCRIPTION = "Notifications for new available coupons"
        private const val PREFS_NAME = "notification_prefs"
        private const val PREF_SEEN_IDS = "seen_ids"
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

    suspend fun showCouponNotification(
        notificationId: String,
        businessName: String,
        title: String,
        discount: Int,
        imageUrl: String,
    ) {
        if (hasSeen(notificationId)) {
            return
        }

        val bitmap = downloadImage(imageUrl)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val requestCode = notificationId.hashCode()
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                requestCode,
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
                    setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null as Bitmap?)
                    )
                }
            }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(requestCode, builder.build())
        markSeen(notificationId)
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

    private fun hasSeen(notificationId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val seen = prefs.getStringSet(PREF_SEEN_IDS, emptySet()).orEmpty()
        return seen.contains(notificationId)
    }

    private fun markSeen(notificationId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val seen = prefs.getStringSet(PREF_SEEN_IDS, emptySet()).orEmpty().toMutableSet()
        seen.add(notificationId)
        prefs.edit().putStringSet(PREF_SEEN_IDS, seen).apply()
    }
}
