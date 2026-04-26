package com.generativecity.wallet

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.generativecity.wallet.data.repository.AppContainer
import com.generativecity.wallet.ui.navigation.AppNavGraph
import com.generativecity.wallet.ui.theme.GenerativeCityWalletTheme
import com.generativecity.wallet.viewmodel.AppViewModelFactory
import com.generativecity.wallet.workers.NotificationInboxSyncWorker
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()
        NotificationInboxSyncWorker.schedule(this)

        val container = AppContainer(applicationContext)
        val factory = AppViewModelFactory(container)
        registerFcmToken(container)

        setContent {
            GenerativeCityWalletTheme {
                AppNavGraph(factory = factory)
            }
        }
    }

    private fun registerFcmToken(container: AppContainer) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            lifecycleScope.launch {
                runCatching { container.notificationsRepository.registerDeviceToken(token) }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Already granted
            } else {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
