package com.generativecity.wallet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.generativecity.wallet.viewmodel.ProfileUiState

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onLogout: () -> Unit
) {
    val inventory = state.inventory

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(60.dp))
                
                // Profile Header Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "My Profile",
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            "Gold Tier Resident",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFF97316),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Surface(
                        modifier = Modifier.size(64.dp).shadow(4.dp, CircleShape),
                        shape = CircleShape,
                        color = Color(0xFFF97316)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "JD",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileStatCard(
                        value = "${inventory?.coins ?: 0}",
                        label = "City Coins",
                        icon = Icons.Default.MonetizationOn,
                        color = Color(0xFFF97316),
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        value = "${inventory?.streakDays ?: 1}",
                        label = "Day Streak",
                        icon = Icons.Default.Whatshot,
                        color = Color(0xFFF97316),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Settings Sections
            item {
                SectionHeader("Account Settings")
                SettingsGroup {
                    SettingsItem(Icons.Default.Person, "Personal Information", "Edit your profile details")
                    SettingsItem(Icons.Default.LocationOn, "Manage My Locations", "3 saved addresses")
                    SettingsItem(Icons.Default.Notifications, "Notification Preferences", "Smart alerts enabled")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionHeader("Security & Privacy")
                SettingsGroup {
                    SettingsItem(Icons.Default.Lock, "Security Center", "Biometrics, Password")
                    SettingsItem(Icons.Default.Shield, "Privacy Settings", "Manage your data sharing")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionHeader("Support")
                SettingsGroup {
                    SettingsItem(Icons.Default.Help, "Help Center", "FAQs and support chat")
                    SettingsItem(Icons.Default.Info, "About Generative City", "v1.2.4")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFEF4444)),
                    border = BorderStroke(1.dp, Color(0xFFFED7D7))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun ProfileStatCard(value: String, label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black,
        color = Color(0xFF0F172A),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFFFF7ED), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFFF97316), modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFCBD5E1))
    }
}
