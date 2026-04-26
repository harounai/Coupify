package com.generativecity.wallet.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.generativecity.wallet.data.repository.AppContainer
import java.util.concurrent.TimeUnit

class NotificationInboxSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val container = AppContainer(applicationContext)

        return try {
            val inbox = container.notificationsRepository.inbox()
            inbox
                .filter { it.status == "pending" }
                .forEach { item ->
                    container.notificationHelper.showCouponNotification(
                        notificationId = item.id,
                        businessName = item.business_name.ifBlank { "New offer" },
                        title = item.title,
                        discount = item.discount_percent,
                        imageUrl = item.image_url,
                    )
                }
            Result.success()
        } catch (e: IllegalStateException) {
            // User not logged in (missing token) is expected; no retry needed.
            if (e.message?.contains("Missing session token") == true) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "notification-inbox-sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<NotificationInboxSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest,
            )
        }
    }
}
