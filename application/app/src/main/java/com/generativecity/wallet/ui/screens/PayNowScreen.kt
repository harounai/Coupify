package com.generativecity.wallet.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.generativecity.wallet.data.remote.CouponInstanceDto
import kotlinx.coroutines.delay

@Composable
fun PayNowScreen(
    coupon: CouponInstanceDto,
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    var isPaying by remember { mutableStateOf(false) }
    var paid by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Spacer(modifier = Modifier.height(38.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Checkout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth().shadow(14.dp, RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Pay now to lock in a better deal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "${coupon.template.title} @ ${coupon.business.name}",
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFFFF7ED))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFF97316), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CreditCard, null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Instant Pay", fontWeight = FontWeight.Black)
                            Text("+5% OFF bonus applied", color = Color(0xFF9A3412), fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "${(coupon.discount_percent + 5).coerceAtMost(100)}% OFF",
                            color = Color(0xFFF97316),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("Payment method", fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Visa •••• 4242", fontWeight = FontWeight.Bold)
                            Text("This is a fake checkout screen (local demo)", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = paid,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFFECFDF3), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color(0xFF10B981))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Payment successful", color = Color(0xFF065F46), fontWeight = FontWeight.Black)
                }
            }

            Button(
                onClick = {
                    if (isPaying || paid) return@Button
                    isPaying = true
                },
                modifier = Modifier.fillMaxWidth().height(62.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                enabled = !isPaying && !paid
            ) {
                if (isPaying) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Processing…", fontWeight = FontWeight.Black)
                } else {
                    Text("Confirm Pay Now", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }

    LaunchedEffect(isPaying) {
        if (!isPaying) return@LaunchedEffect
        delay(1100)
        paid = true
        delay(500)
        onPaymentSuccess()
    }
}

