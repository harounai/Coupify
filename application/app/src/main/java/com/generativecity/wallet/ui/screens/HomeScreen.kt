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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.generativecity.wallet.data.remote.CouponInstanceDto
import com.generativecity.wallet.ui.components.OfferCard
import com.generativecity.wallet.viewmodel.HomeUiState
import com.generativecity.wallet.viewmodel.NotificationsUiState
import com.generativecity.wallet.viewmodel.WalletUiState
import kotlin.math.ceil

@Composable
fun HomeScreen(
    homeState: HomeUiState,
    walletState: WalletUiState,
    notificationsState: NotificationsUiState,
    onUseOffer: (String) -> Unit,
    onDeclineNotification: (String) -> Unit,
    onAcceptNotification: (String) -> Unit,
    onInstantPayNotification: (String) -> Unit,
    onOpenNotificationCoupon: (String) -> Unit,
    onClaimFromAi: (CouponInstanceDto) -> Unit,
    onPayNowLive: (String) -> Unit,
    onApplyDouble: (String) -> Unit = {},
    onApplyTime: (String) -> Unit = {}
) {
    val dismissedLiveIds = remember { mutableStateListOf<String>() }
    val acceptedLiveIds = remember { mutableStateListOf<String>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        if (homeState.isLoading && homeState.feed == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFF97316)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            homeState.error?.let { msg ->
                item {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Failed to load offers", fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(msg, color = Color(0xFF9A3412), style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Tip: make sure emulator can open http://10.0.2.2:8000/health",
                                color = Color(0xFF9A3412),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            item {
                HomeHeader(
                    userName = homeState.feed?.user?.display_name ?: "—",
                    streakDays = homeState.feed?.streak?.current_days ?: 0
                )
            }

            // 1. CLAIMED REWARDS (Pinned at top)
            val claimedFromBackend = homeState.feed?.claimed_rewards_today ?: emptyList()
            val claimedOffers = claimedFromBackend.map { c ->
                OfferEntity(
                    id = c.id,
                    userId = homeState.feed?.user?.id ?: "",
                    businessName = c.business.name,
                    title = "${c.template.title} @ ${c.business.name}",
                    discountPercent = c.discount_percent,
                    status = OfferStatus.SAVED,
                    expiryEpochMillis = System.currentTimeMillis() + (c.template.duration_hours * 60 * 60 * 1000),
                    distanceKm = c.business.distance_km ?: 0.0,
                    category = c.business.category,
                    imageUrl = c.business.image_url,
                    createdEpochMillis = System.currentTimeMillis()
                )
            }
            if (claimedOffers.isNotEmpty()) {
                item { SectionTitle("Claimed Rewards") }
                items(claimedOffers, key = { "claimed_${it.id}" }) { offer ->
                    val activatedAt = walletState.qrActivatedAtEpochMillis[offer.id]
                    val remaining = if (activatedAt == null) null else {
                        val expiresAt = activatedAt + 15 * 60 * 1000L
                        ceil(((expiresAt - System.currentTimeMillis()).coerceAtLeast(0)).toDouble() / 1000.0).toLong()
                    }
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        ClaimedRewardCard(
                            offer = offer,
                            onOpen = { onUseOffer(offer.id) },
                            qrRemainingSeconds = remaining
                        )
                    }
                }
            }

            // 2. LIVE OPPORTUNITIES (AI-ranked)
            val liveFromAi = (homeState.feed?.live_opportunities ?: emptyList())
                .filterNot { dismissedLiveIds.contains(it.id) || acceptedLiveIds.contains(it.id) }
                .take(3)
            if (liveFromAi.isNotEmpty()) {
                item { SectionTitle("Live Opportunities", isHighPriority = true) }
                items(liveFromAi, key = { "ai_${it.id}" }) { coupon ->
                    val currentUserId = homeState.feed?.user?.id ?: ""
                    val offer = OfferEntity(
                        id = coupon.id,
                        userId = currentUserId,
                        title = "${coupon.template.title} @ ${coupon.business.name}",
                        discountPercent = coupon.discount_percent,
                        distanceKm = coupon.business.distance_km ?: 0.0,
                        createdEpochMillis = System.currentTimeMillis(),
                        expiryEpochMillis = System.currentTimeMillis() + (coupon.template.duration_hours * 60 * 60 * 1000),
                        businessName = coupon.business.name,
                        category = coupon.business.category,
                        imageUrl = coupon.business.image_url,
                        status = OfferStatus.ACTIVE
                    )
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        LiveOpportunityCard(
                            offer = offer,
                            onDecline = { dismissedLiveIds.add(coupon.id) },
                            onAccept = {
                                acceptedLiveIds.add(coupon.id)
                                onClaimFromAi(coupon)
                            },
                            onPayNow = { onPayNowLive(coupon.id) }
                        )
                    }
                }
            } else {
                item {
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                        Text(
                            "No live opportunities yet. If this persists, check backend connectivity.",
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // 3. OFFER OF THE DAY
            item {
                SectionTitle("Offer of the Day")
                val otd = homeState.feed?.offer_of_the_day
                if (otd != null) {
                    val featuredOffer = OfferEntity(
                        id = otd.business.id,
                        userId = homeState.feed?.user?.id ?: "",
                        businessName = otd.business.name,
                        title = "${otd.template.title} @ ${otd.business.name}",
                        discountPercent = otd.discount_percent,
                        status = OfferStatus.ACTIVE,
                        expiryEpochMillis = System.currentTimeMillis() + (otd.template.duration_hours * 60 * 60 * 1000),
                        distanceKm = otd.business.distance_km ?: 0.0,
                        category = otd.business.category,
                        createdEpochMillis = System.currentTimeMillis()
                    )
                    FeaturedDealCard(offer = featuredOffer, onUse = { onOpenNotificationCoupon(featuredOffer.id) })
                }
            }

            // 4. NEW IN TOWN
            item {
                SectionTitle("New in Town", showBadge = true)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val newBiz = homeState.feed?.new_in_town ?: emptyList()
                    items(newBiz) { biz ->
                        NewInTownCard(
                            com.generativecity.wallet.data.model.Business(
                                id = biz.id,
                                name = biz.name,
                                category = biz.category,
                                distanceKm = biz.distance_km ?: 0.0,
                                demandLevel = biz.demand_level ?: 50,
                                imageUrl = biz.image_url
                            ),
                            onOpen = { onOpenNotificationCoupon(biz.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveOpportunityCard(
    offer: OfferEntity,
    onDecline: () -> Unit,
    onAccept: () -> Unit,
    onPayNow: () -> Unit
) {
    var acceptedAnimating by remember { mutableStateOf(false) }
    val checkScale by animateFloatAsState(
        targetValue = if (acceptedAnimating) 1f else 0.6f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = ""
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (acceptedAnimating) 0.85f else 1f,
        animationSpec = tween(180),
        label = ""
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(18.dp).graphicsLayer(alpha = cardAlpha)) {
            if (offer.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = offer.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFFF7ED), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Bolt, null, tint = Color(0xFFF97316), modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "LIVE OPPORTUNITY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF97316)
                    )
                    Text(offer.businessName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    "${offer.discountPercent}% OFF",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF97316),
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(offer.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = acceptedAnimating,
                enter = fadeIn(tween(150)) + scaleIn(tween(220)),
                exit = fadeOut(tween(120))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .scale(checkScale)
                            .background(Color(0xFFECFDF3), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Accepted — added to Claimed Rewards", color = Color(0xFF065F46), fontWeight = FontWeight.Bold)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text("Decline", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        acceptedAnimating = true
                        onAccept()
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                ) {
                    Text("Accept", fontWeight = FontWeight.Black, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onPayNow,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
                Text("Pay Now (+5% OFF)", fontWeight = FontWeight.Black, fontSize = 15.sp)
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
fun ClaimedRewardCard(offer: OfferEntity, onOpen: () -> Unit, qrRemainingSeconds: Long?) {
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
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Valid until end of day",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.SemiBold
            )
            if (qrRemainingSeconds != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = Color(0xFFFFF7ED), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        "QR active • ${qrRemainingSeconds / 60}:${String.format("%02d", qrRemainingSeconds % 60)} remaining",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = Color(0xFFC2410C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
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
                model = offer.imageUrl.ifBlank { "https://images.unsplash.com/photo-1555396273-367ea4eb4db5" },
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
