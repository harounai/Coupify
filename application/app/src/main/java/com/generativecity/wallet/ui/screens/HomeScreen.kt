package com.generativecity.wallet.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.generativecity.wallet.data.local.OfferEntity
import com.generativecity.wallet.data.model.OfferStatus
import com.generativecity.wallet.data.remote.MockData
import com.generativecity.wallet.ui.components.OfferCard
import com.generativecity.wallet.viewmodel.NotificationsUiState
import com.generativecity.wallet.viewmodel.WalletUiState

@Composable
fun HomeScreen(
    walletState: WalletUiState,
    notificationsState: NotificationsUiState,
    onUseOffer: (String) -> Unit,
    onDeclineNotification: (String) -> Unit,
    onAcceptNotification: (String) -> Unit,
    onInstantPayNotification: (String) -> Unit,
    onOpenNotificationCoupon: (String) -> Unit,
    onApplyDouble: (String) -> Unit = {},
    onApplyTime: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                HomeHeader(
                    userName = "Alex",
                    streakDays = 7
                )
            }

            // 1. LIVE OPPORTUNITIES (Highest Priority)
            val liveNotifications = notificationsState.notifications.filter { it.status == OfferStatus.ACTIVE }
            if (liveNotifications.isNotEmpty()) {
                item { SectionTitle("Live Opportunities", isHighPriority = true) }
                items(liveNotifications, key = { "notif_${it.id}" }) { notification ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        ModernNotificationCard(
                            notification = notification,
                            decisionSeconds = notificationsState.decisionSecondsRemaining[notification.id] ?: 0,
                            isProcessing = notificationsState.paymentInProgressOfferId == notification.id,
                            doubleCount = notificationsState.doubleOrNothingCount,
                            timeCount = notificationsState.timeExtensionCount,
                            onDecline = { onDeclineNotification(notification.id) },
                            onAccept = { onAcceptNotification(notification.id) },
                            onInstantPay = { onInstantPayNotification(notification.id) },
                            onApplyDouble = { onApplyDouble(notification.id) },
                            onApplyTime = { onApplyTime(notification.id) }
                        )
                    }
                }
            }

            // 2. CLAIMED REWARDS (Pinned at top)
            val claimedOffers = (walletState.activeOffers + walletState.savedOffers + walletState.redeemedOffers).filter { 
                it.status == OfferStatus.NOTIFICATION_ACCEPTED || it.status == OfferStatus.INSTANT_PAID || it.status == OfferStatus.SAVED
            }
            if (claimedOffers.isNotEmpty()) {
                item { SectionTitle("Claimed Rewards") }
                items(claimedOffers, key = { "claimed_${it.id}" }) { offer ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        ClaimedRewardCard(offer = offer, onOpen = { onUseOffer(offer.id) })
                    }
                }
            }

            // 3. OFFER OF THE DAY
            item {
                SectionTitle("Offer of the Day")
                val featuredOffer = MockData.businesses.first().let {
                    OfferEntity(
                        id = it.id, // Using real biz id to match detail logic
                        userId = 0,
                        businessName = it.name,
                        title = "Exclusive Weekend Pass",
                        discountPercent = 40,
                        status = OfferStatus.ACTIVE,
                        expiryEpochMillis = System.currentTimeMillis() + 3600000,
                        distanceKm = it.distanceKm,
                        category = it.category,
                        createdEpochMillis = System.currentTimeMillis()
                    )
                }
                FeaturedDealCard(offer = featuredOffer, onUse = { onOpenNotificationCoupon(featuredOffer.id) })
            }

            // 4. NEW IN TOWN
            item {
                SectionTitle("New in Town", showBadge = true)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(MockData.businesses.takeLast(3)) { biz ->
                        NewInTownCard(biz, onOpen = { onOpenNotificationCoupon(biz.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun HomeHeader(userName: String, streakDays: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Good morning, $userName",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF64748B)
                )
                Text(
                    "Your Dashboard",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    letterSpacing = (-1).sp
                )
            }
            Surface(
                modifier = Modifier.size(52.dp).shadow(12.dp, CircleShape),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color(0xFFF97316))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HeaderBadge(Icons.Default.Whatshot, "$streakDays Day Streak", Color(0xFFF97316))
        }
    }
}

@Composable
fun ClaimedRewardCard(offer: OfferEntity, onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFECFDF3), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(offer.businessName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Verified Coupon Available", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                }
                Text(
                    "${offer.discountPercent}% OFF",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF97316),
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
                Icon(Icons.Default.QrCode, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Display QR-Code", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HeaderBadge(icon: ImageVector, text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp)
        }
    }
}

@Composable
fun FeaturedDealCard(offer: OfferEntity, onUse: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .height(200.dp)
            .clickable { onUse() },
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Surface(
                    color = Color(0xFFF97316),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "OFFER OF THE DAY",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    offer.businessName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "TAP TO EXPLORE",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NewInTownCard(business: com.generativecity.wallet.data.model.Business, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.width(260.dp).clickable { onOpen() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp)) {
                AsyncImage(
                    model = business.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                    color = Color(0xFFF97316),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "NEW",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(business.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${business.category.uppercase()} · ${business.distanceKm} km",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, showBadge: Boolean = false, isHighPriority: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = if (isHighPriority) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = if (isHighPriority) Color(0xFFF97316) else Color(0xFF0F172A)
        )
        if (showBadge) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = Color(0xFFF97316).copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Text(
                    "3",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = Color(0xFFF97316),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ModernNotificationCard(
    notification: OfferEntity,
    decisionSeconds: Long,
    isProcessing: Boolean,
    doubleCount: Int,
    timeCount: Int,
    onDecline: () -> Unit,
    onAccept: () -> Unit,
    onInstantPay: () -> Unit,
    onApplyDouble: () -> Unit,
    onApplyTime: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = ""
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color(0xFFF97316).copy(alpha = glowAlpha)
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFFFF7ED), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Bolt, null, tint = Color(0xFFF97316), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "LIMITED OPPORTUNITY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF97316)
                    )
                    Text(
                        notification.businessName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "${decisionSeconds / 60}:${String.format("%02d", decisionSeconds % 60)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(notification.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    "${notification.discountPercent}% OFF",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFF97316),
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Standard Offer", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFFF97316))
            } else {
                Button(
                    onClick = onInstantPay,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text("PAY NOW (+5% BONUS)", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text("Dismiss", color = Color(0xFF64748B))
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1.5f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF1E293B))
                    ) {
                        Text("Accept", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
