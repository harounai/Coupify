package com.generativecity.wallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.generativecity.wallet.data.model.UserRole

@Composable
fun LoginScreen(
    onUserLogin: (String, List<String>, Int) -> Unit,
    onCompanyLogin: (String, String, String, Int) -> Unit
) {
    var role by remember { mutableStateOf(UserRole.USER) }
    var username by remember { mutableStateOf("") }

    var selectedInterests by remember { mutableStateOf(setOf<String>()) }
    var explorationPreference by remember { mutableStateOf(50f) }

    var businessName by remember { mutableStateOf("") }
    var businessCategory by remember { mutableStateOf("food") }
    var maxDiscount by remember { mutableStateOf("20") }

    val interests = listOf("coffee", "fitness", "food", "tech", "wellness")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // App Logo Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0052FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.RocketLaunch, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Generative City",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
            Text(
                "Personalized AI-Driven Wallet",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(40.dp))
            
            // Modern Role Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp)
            ) {
                RoleButton(
                    modifier = Modifier.weight(1f),
                    label = "Customer",
                    icon = Icons.Default.Person,
                    isSelected = role == UserRole.USER,
                    onClick = { role = UserRole.USER }
                )
                RoleButton(
                    modifier = Modifier.weight(1f),
                    label = "Business",
                    icon = Icons.Default.Business,
                    isSelected = role == UserRole.COMPANY,
                    onClick = { role = UserRole.COMPANY }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        if (role == UserRole.USER) "Onboarding Survey" else "Business Registration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Your Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (role == UserRole.USER) {
                        Text("Select Interests", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            interests.forEach { interest ->
                                FilterChip(
                                    selected = selectedInterests.contains(interest),
                                    onClick = {
                                        selectedInterests = if (selectedInterests.contains(interest)) {
                                            selectedInterests - interest
                                        } else {
                                            selectedInterests + interest
                                        }
                                    },
                                    label = { Text(interest) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Exploration Style", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${explorationPreference.toInt()}%", color = Color(0xFF0052FF), fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = explorationPreference,
                            onValueChange = { explorationPreference = it },
                            valueRange = 0f..100f
                        )
                    } else {
                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Business Legal Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = businessCategory,
                            onValueChange = { businessCategory = it },
                            label = { Text("Business Category") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = maxDiscount,
                            onValueChange = { maxDiscount = it.filter { char -> char.isDigit() } },
                            label = { Text("Max Offer Discount (%)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (role == UserRole.USER) {
                        onUserLogin(
                            username.ifBlank { "Guest User" },
                            selectedInterests.toList(),
                            explorationPreference.toInt()
                        )
                    } else {
                        onCompanyLogin(
                            username.ifBlank { "Merchant" },
                            businessName.ifBlank { "My Business" },
                            businessCategory.ifBlank { "food" },
                            maxDiscount.toIntOrNull() ?: 20
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052FF))
            ) {
                Text(
                    if (role == UserRole.USER) "Enter Wallet" else "Access Dashboard",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.ArrowForward, null)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun RoleButton(
    modifier: Modifier,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                null,
                tint = if (isSelected) Color(0xFF0052FF) else Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF0F172A) else Color(0xFF64748B)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
