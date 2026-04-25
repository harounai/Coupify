package com.generativecity.wallet.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.generativecity.wallet.viewmodel.RouletteUiState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RouletteScreen(
    state: RouletteUiState,
    onSpin: () -> Unit
) {
    var showResultPopup by remember { mutableStateOf(false) }
    var isSpinning by remember { mutableStateOf(false) }
    
    val animatedRotation by animateFloatAsState(
        targetValue = state.finalDegree,
        animationSpec = if (state.finalDegree == 0f) snap() else tween(durationMillis = 4000, easing = CubicBezierEasing(0.2f, 0.0f, 0.2f, 1.0f)),
        label = "roulette-rotation",
        finishedListener = {
            isSpinning = false
            if (state.lastResult != "Spin to try your luck") {
                showResultPopup = true
            }
        }
    )

    // Removed blur effect to keep the page clear and not blurry
    val blurAmount = 0.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A0F02), Color(0xFF2D1B05))
                )
            )
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFFF97316).copy(alpha = 0.08f),
            modifier = Modifier.size(400.dp).offset(x = 100.dp, y = (-50).dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    "Lucky Wheel",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Text(
                    "Unlock premium city rewards daily",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(300.dp)
                ) {
                    // Modern Neon Glow - Orange theme
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .shadow(
                                elevation = 60.dp,
                                shape = CircleShape,
                                spotColor = Color(0xFFF97316),
                                ambientColor = Color(0xFFF97316)
                            )
                    )

                    Canvas(
                        modifier = Modifier
                            .size(270.dp)
                            .rotate(animatedRotation)
                            .blur(blurAmount)
                            .clip(CircleShape)
                    ) {
                        val segments = listOf(
                            "WIN", "MISS", "WIN", "MISS", 
                            "WIN", "MISS", "WIN", "MISS"
                        )
                        val labels = listOf(
                            "BOOST", "LOST", "COUPON", "LOST", 
                            "FREEZE", "LOST", "TIME", "LOST"
                        )
                        val sweep = 360f / segments.size
                        
                        segments.forEachIndexed { index, type ->
                            val isWin = type == "WIN"
                            
                            val brush = if (isWin) {
                                Brush.radialGradient(listOf(Color(0xFFFB923C), Color(0xFFEA580C)))
                            } else {
                                Brush.radialGradient(listOf(Color(0xFF451A03), Color(0xFF1C0D02)))
                            }

                            drawArc(
                                brush = brush,
                                startAngle = index * sweep,
                                sweepAngle = sweep,
                                useCenter = true
                            )
                            
                            drawArc(
                                color = Color.White.copy(alpha = 0.05f),
                                startAngle = index * sweep,
                                sweepAngle = sweep,
                                useCenter = true,
                                style = Stroke(width = 2f)
                            )
                            
                            drawIntoCanvas { canvas ->
                                val angle = (index * sweep + sweep / 2) * (PI / 180f)
                                val radius = size.width * 0.32f
                                val x = (size.width / 2) + (radius * cos(angle)).toFloat()
                                val y = (size.height / 2) + (radius * sin(angle)).toFloat()
                                
                                canvas.nativeCanvas.save()
                                canvas.nativeCanvas.rotate(index * sweep + sweep / 2 + 90f, x, y)
                                val p = android.graphics.Paint().apply {
                                    color = if (isWin) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#78350F")
                                    textSize = 34f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                    isAntiAlias = true
                                }
                                canvas.nativeCanvas.drawText(labels[index], x, y, p)
                                canvas.nativeCanvas.restore()
                            }
                        }
                        
                        drawCircle(color = Color.White, radius = 32.dp.toPx())
                        drawCircle(
                            brush = Brush.verticalGradient(listOf(Color(0xFFF97316), Color(0xFFC2410C))),
                            radius = 28.dp.toPx()
                        )
                    }
                    
                    Icon(
                        Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (-18).dp)
                            .shadow(12.dp, CircleShape)
                            .rotate(180f)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Fixed the Button appearance when disabled
                Button(
                    onClick = {
                        isSpinning = true
                        onSpin()
                    },
                    enabled = state.canSpin && !isSpinning,
                    modifier = Modifier
                        .fillMaxWidth(0.7f) // Slightly larger width for better UI
                        .height(64.dp)
                        .shadow(if (state.canSpin && !isSpinning) 12.dp else 0.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFF97316)),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF97316),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF475569), // Better disabled visibility
                        disabledContentColor = Color(0xFF94A3B8)
                    )
                ) {
                    Text(
                        if (state.canSpin && !isSpinning) "SPIN" 
                        else if (isSpinning) "SPINNING..."
                        else "COME BACK TOMORROW",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 16.sp
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "My Inventory",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                InventoryGrid(state)
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showResultPopup) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showResultPopup = false }) {
            val isWin = !state.lastResult.contains("Better luck")
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .padding(16.dp)
                    .shadow(40.dp, RoundedCornerShape(32.dp), spotColor = if (isWin) Color(0xFF10B981) else Color(0xFFEF4444))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                if (isWin) Color(0xFF10B981).copy(alpha = 0.1f) 
                                else Color(0xFFEF4444).copy(alpha = 0.1f), 
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isWin) Icons.Default.EmojiEvents else Icons.Default.SentimentVeryDissatisfied,
                            null,
                            tint = if (isWin) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        if (isWin) "Big Win!" else "So Close!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        state.lastResult,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { showResultPopup = false },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text("AWESOME", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryGrid(state: RouletteUiState) {
    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InventoryItem(Icons.Default.ConfirmationNumber, "Coupons", state.inventory?.freeCouponCount ?: 0, Modifier.weight(1f))
            InventoryItem(Icons.Default.Timer, "Time Ext.", state.inventory?.timeExtensionCount ?: 0, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InventoryItem(Icons.Default.AcUnit, "Freezers", state.inventory?.streakFreezerCount ?: 0, Modifier.weight(1f))
            InventoryItem(Icons.Default.DoubleArrow, "Boosts", state.inventory?.doubleOrNothingCount ?: 0, Modifier.weight(1f))
        }
    }
}

@Composable
fun InventoryItem(
    icon: ImageVector,
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Color(0xFFF97316), modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = Color(0xFF94A3B8))
            Text(
                count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}
