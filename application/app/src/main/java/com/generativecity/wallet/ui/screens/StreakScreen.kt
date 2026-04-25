package com.generativecity.wallet.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun StreakScreen() {
    val currentStreak = 7
    val daysInMonth = 30
    
    var showIgnition by remember { mutableStateOf(false) }
    var redeemed7Day by remember { mutableStateOf(false) }
    var redeemed30Day by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(300)
        showIgnition = true
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(60.dp))
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        "Streak",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        "Your daily momentum",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Premium Streak Ignition Card - Smaller and Orange
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    StreakIgnitionCard(currentStreak, showIgnition)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

            item {
                Text(
                    "Rewards Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Grid of days - Smaller and non-scrollable
                Box(modifier = Modifier.padding(horizontal = 24.dp).height(200.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = false
                    ) {
                        items(daysInMonth) { index ->
                            val day = index + 1
                            val isCompleted = day <= currentStreak
                            val isMilestone = day == 7 || day == 30
                            
                            StreakDayCell(day, isCompleted, isMilestone)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 7-Day Reward Card
                    CompactRewardCard(
                        title = "7-Day Reward",
                        reward = "Free 10% Extra Discount",
                        isUnlocked = currentStreak >= 7,
                        isRedeemed = redeemed7Day,
                        onRedeem = { redeemed7Day = true }
                    )

                    // 30-Day Reward Card
                    CompactRewardCard(
                        title = "30-Day Reward",
                        reward = "Ultimate Premium Pass",
                        isUnlocked = currentStreak >= 30,
                        isRedeemed = redeemed30Day,
                        onRedeem = { redeemed30Day = true }
                    )
                }
            }
        }
    }
}

@Composable
fun StreakIgnitionCard(streak: Int, isIgnited: Boolean) {
    val pulseScale by animateFloatAsState(
        targetValue = if (isIgnited) 1.02f else 1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = ""
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            },
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0F172A)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "$streak Days",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    "STREAK ACTIVE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFF97316),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF97316).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Whatshot,
                    contentDescription = null,
                    tint = Color(0xFFF97316),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun StreakDayCell(day: Int, isCompleted: Boolean, isMilestone: Boolean) {
    Surface(
        modifier = Modifier.aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        color = when {
            isCompleted -> Color(0xFFF97316)
            isMilestone -> Color(0xFFFFF7ED)
            else -> Color.White
        },
        border = if (!isCompleted && isMilestone) BorderStroke(1.dp, Color(0xFFF97316)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isCompleted) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            } else if (isMilestone) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFF97316), modifier = Modifier.size(14.dp))
            } else {
                Text(
                    "$day",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CompactRewardCard(
    title: String,
    reward: String,
    isUnlocked: Boolean,
    isRedeemed: Boolean,
    onRedeem: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isRedeemed) Color(0xFFECFDF3) else if (isUnlocked) Color(0xFFFFF7ED) else Color(0xFFF1F5F9),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isRedeemed) Icons.Default.CheckCircle else Icons.Default.Redeem,
                    null,
                    tint = if (isRedeemed) Color(0xFF10B981) else if (isUnlocked) Color(0xFFF97316) else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(reward, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
            }
            
            if (isUnlocked && !isRedeemed) {
                Button(
                    onClick = onRedeem,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("REDEEM", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            } else if (isRedeemed) {
                Text("CLAIMED", color = Color(0xFF10B981), fontWeight = FontWeight.Black, fontSize = 12.sp)
            } else {
                Icon(Icons.Default.Lock, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(18.dp))
            }
        }
    }
}
