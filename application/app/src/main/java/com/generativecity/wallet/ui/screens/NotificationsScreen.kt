package com.generativecity.wallet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.generativecity.wallet.data.local.OfferEntity
import com.generativecity.wallet.data.model.OfferStatus
import com.generativecity.wallet.utils.DateTimeUtils
import com.generativecity.wallet.viewmodel.NotificationsUiState
import kotlin.math.min

@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onDecline: (String) -> Unit,
    onAccept: (String) -> Unit,
    onInstantPay: (String) -> Unit,
    onOpenCoupon: (String) -> Unit
) {
    var selectedNotification by remember { mutableStateOf<OfferEntity?>(null) }

    fun formatDecisionTime(seconds: Long): String {
        val min = seconds / 60
        val sec = seconds % 60
        return String.format("%02d:%02d", min, sec)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Notifications", style = MaterialTheme.typography.headlineMedium)
            Text("Context-aware offers are refreshed every 5 minutes")
        }

        items(state.notifications, key = { it.id }) { notification ->
            val decisionSeconds = state.decisionSecondsRemaining[notification.id] ?: 0
            val canDecide = decisionSeconds > 0 && notification.status == OfferStatus.ACTIVE
            val canOpenCoupon = notification.status == OfferStatus.NOTIFICATION_ACCEPTED || notification.status == OfferStatus.INSTANT_PAID
            val instantPayPercent = min(60, notification.discountPercent + 5)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(notification.title, style = MaterialTheme.typography.titleMedium)
                    Text("Coupon ${notification.discountPercent}% • Instant Pay $instantPayPercent%")
                    Text("Expires ${DateTimeUtils.formatExpiry(notification.expiryEpochMillis)}")
                    Text(
                        if (canDecide) "Decision time left: ${formatDecisionTime(decisionSeconds)}" else "Decision window closed",
                        color = if (canDecide) Color(0xFFB42318) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (state.paymentInProgressOfferId == notification.id) {
                        Text("Processing payment...", color = MaterialTheme.colorScheme.primary)
                    }

                    if (canDecide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onDecline(notification.id) }) { Text("Decline") }
                            Button(onClick = { onAccept(notification.id) }) { Text("Accept") }
                            Button(onClick = { onInstantPay(notification.id) }) { Text("Instant Pay") }
                        }
                    }

                    if (canOpenCoupon) {
                        Spacer(modifier = Modifier)
                        Button(onClick = { selectedNotification = notification }, modifier = Modifier.fillMaxWidth()) {
                            Text("Open Coupon")
                        }
                    }
                }
            }
        }
    }

    selectedNotification?.let { notification ->
        val instantPayPercent = min(60, notification.discountPercent + 5)
        AlertDialog(
            onDismissRequest = { selectedNotification = null },
            title = { Text(notification.businessName) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(notification.title)
                    Text("Coupon: ${notification.discountPercent}%")
                    Text("Instant Pay option: $instantPayPercent%")
                    Text("Tap Open QR to generate and redeem this offer")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onOpenCoupon(notification.id)
                        selectedNotification = null
                    }
                ) {
                    Text("Open QR")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedNotification = null }) {
                    Text("Close")
                }
            }
        )
    }
}
