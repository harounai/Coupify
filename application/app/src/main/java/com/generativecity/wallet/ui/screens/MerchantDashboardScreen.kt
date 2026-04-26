package com.generativecity.wallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.generativecity.wallet.viewmodel.MerchantDashboardUiState

@Composable
fun MerchantDashboardScreen(
    state: MerchantDashboardUiState,
    onSaveRules: (maxDiscount: Int, minDiscount: Int, goal: String, couponsPerDay: Int, couponsTotal: Int, products: List<String>) -> Unit,
    onSimulateLowDemand: () -> Unit,
    onLogout: () -> Unit
) {
    var maxDiscount by remember { mutableIntStateOf(state.rules?.max_discount_percent ?: 20) }
    var minDiscount by remember { mutableIntStateOf(state.rules?.min_discount_percent ?: 5) }
    var goal by remember { mutableStateOf(state.rules?.goal ?: "FILL_QUIET_HOURS") }
    var couponsPerDay by remember { mutableIntStateOf(state.rules?.coupons_per_day ?: 50) }
    var couponsTotal by remember { mutableIntStateOf(state.rules?.coupons_total ?: 1000) }
    var productsCsv by remember { mutableStateOf((state.rules?.products ?: emptyList()).joinToString(",")) }

    val orange = Color(0xFFF97316)
    val slate = Color(0xFF0F172A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFFFF7ED))))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Business Hub",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = slate
                    )
                    Text(
                        "Business: ${state.selectedBusinessId ?: "-"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color(0xFFEF4444))
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(color = orange)
            }
            if (state.error != null) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .aspectRatio(1f)
                                .background(Color(0xFFFFF7ED), RoundedCornerShape(14.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = orange,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Column {
                            Text("Merchant Rules", fontWeight = FontWeight.Black, color = slate)
                            Text("Set guardrails, AI generates offers", color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                // Demo preset: good defaults for a hackathon pitch
                                maxDiscount = 25
                                minDiscount = 10
                                goal = "FILL_QUIET_HOURS"
                                couponsPerDay = 50
                                couponsTotal = 1000
                                productsCsv = "coffee,food"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF7ED), contentColor = orange),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Use demo preset", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onSimulateLowDemand,
                            colors = ButtonDefaults.buttonColors(containerColor = slate, contentColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Simulate low demand", fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("Max discount: $maxDiscount%", fontWeight = FontWeight.SemiBold, color = slate)
                    Slider(
                        value = maxDiscount.toFloat(),
                        onValueChange = { maxDiscount = it.toInt() },
                        valueRange = 0f..30f
                    )

                    Text("Min discount: $minDiscount%", fontWeight = FontWeight.SemiBold, color = slate)
                    Slider(
                        value = minDiscount.toFloat(),
                        onValueChange = { minDiscount = it.toInt() },
                        valueRange = 0f..30f
                    )

                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Goal") },
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = productsCsv,
                        onValueChange = { productsCsv = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Products/categories (comma separated)") },
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = couponsPerDay.toString(),
                        onValueChange = { couponsPerDay = it.toIntOrNull() ?: couponsPerDay },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Coupons per day") },
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = couponsTotal.toString(),
                        onValueChange = { couponsTotal = it.toIntOrNull() ?: couponsTotal },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Total coupons budget") },
                        shape = RoundedCornerShape(14.dp)
                    )

                    Button(
                        onClick = {
                            val products = productsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            onSaveRules(maxDiscount, minDiscount, goal, couponsPerDay, couponsTotal, products)
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = orange, contentColor = Color.White)
                    ) {
                        Text("Save rules", fontWeight = FontWeight.Black)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Today’s performance", fontWeight = FontWeight.Black, color = slate)
                    Text("Impressions: ${state.stats?.impressions ?: 0}", color = Color(0xFF334155))
                    Text("Accepts: ${state.stats?.accepts ?: 0}", color = Color(0xFF334155))
                    Text("Declines: ${state.stats?.declines ?: 0}", color = Color(0xFF334155))
                    Text("Redemptions: ${state.stats?.redemptions ?: 0}", color = Color(0xFF334155))
                    Text(
                        "Total issued: ${state.rules?.coupons_total_issued ?: 0}/${state.rules?.coupons_total ?: 0}",
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFEF4444))
            ) {
                Text("Logout", fontWeight = FontWeight.Black)
            }
        }
    }
}

