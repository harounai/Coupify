package com.generativecity.wallet.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.generativecity.wallet.data.model.Business
import com.generativecity.wallet.viewmodel.ExploreFilterMode
import com.generativecity.wallet.viewmodel.ExploreUiState

@Composable
fun ExploreScreen(
    state: ExploreUiState,
    onCategoryChanged: (String) -> Unit,
    onDistanceChanged: (Double) -> Unit,
    onFilterModeChanged: (ExploreFilterMode) -> Unit,
    onLocationChanged: (String) -> Unit,
    onOpenBusiness: (String) -> Unit
) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    val couponByCategory = state.coupons.groupBy { it.category }
        .mapValues { (_, coupons) -> coupons.maxBy { it.baseDiscount } }

    val categories = listOf("all", "coffee", "fitness", "food")
    
    val filtered = state.businesses
        .filter { business -> state.selectedCategory == "all" || business.category == state.selectedCategory }
        .filter { business -> business.distanceKm <= state.maxDistanceKm }
        .let { businesses ->
            when (state.filterMode) {
                ExploreFilterMode.COUPONS -> businesses
                    .filter { couponByCategory[it.category] != null }
                    .sortedByDescending { couponByCategory[it.category]?.baseDiscount ?: 0 }
                ExploreFilterMode.ACTIVITY -> businesses.sortedByDescending { it.demandLevel }
                ExploreFilterMode.NEAREST -> businesses.sortedBy { it.distanceKm }
                ExploreFilterMode.PERSONALIZED -> businesses.sortedBy { business ->
                    val preferenceWeight = if (business.category == state.selectedCategory || state.selectedCategory == "all") 0 else 1
                    (preferenceWeight * 0.7) + (business.distanceKm * 0.3)
                }
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Search & Filter Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Spacer(modifier = Modifier.height(60.dp)) // Increased from 16.dp for better readability
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                state.currentLocationName,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF97316)
                            )
                        }
                        Text(
                            "Discover",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    
                    Row {
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Settings", tint = Color(0xFF64748B))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { /* Search */ },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Categories Row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = state.selectedCategory == category
                        val icon = when(category) {
                            "coffee" -> Icons.Default.Coffee
                            "fitness" -> Icons.Default.FitnessCenter
                            "food" -> Icons.Default.Restaurant
                            else -> Icons.Default.Apps
                        }
                        
                        CategoryChip(
                            label = category.replaceFirstChar { it.uppercase() },
                            icon = icon,
                            isSelected = isSelected,
                            onClick = { onCategoryChanged(category) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sorting Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FilterDropdown(
                            currentMode = state.filterMode,
                            onModeChanged = onFilterModeChanged
                        )
                    }
                    
                    Text(
                        "Within ${"%.1f".format(state.maxDistanceKm)}km",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(filtered, key = { it.id }) { business ->
                val coupon = couponByCategory[business.category]
                ModernMerchantCard(
                    business = business,
                    couponDiscount = coupon?.baseDiscount,
                    onClick = { onOpenBusiness(business.id) }
                )
            }
        }
    }

    if (showSettingsDialog) {
        DiscoverySettingsDialog(
            currentLocation = state.currentLocationName,
            currentDistance = state.maxDistanceKm,
            onDismiss = { showSettingsDialog = false },
            onSave = { location, distance ->
                onLocationChanged(location)
                onDistanceChanged(distance)
                showSettingsDialog = false
            }
        )
    }
}

@Composable
fun DiscoverySettingsDialog(
    currentLocation: String,
    currentDistance: Double,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var location by remember { mutableStateOf(currentLocation) }
    var distance by remember { mutableDoubleStateOf(currentDistance) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Discovery Settings", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Column {
                    Text("Search Location", style = MaterialTheme.typography.labelLarge, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = { Text("City or Neighborhood") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Search Radius", style = MaterialTheme.typography.labelLarge, color = Color(0xFF64748B))
                        Text("${"%.1f".format(distance)} km", fontWeight = FontWeight.Bold, color = Color(0xFFF97316))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = distance.toFloat(),
                        onValueChange = { distance = it.toDouble() },
                        valueRange = 0.5f..20f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFF97316), activeTrackColor = Color(0xFFF97316))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(location, distance) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        }
    )
}

@Composable
fun CategoryChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFFF97316) else Color(0xFFF1F5F9),
        contentColor = if (isSelected) Color.White else Color(0xFF64748B)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun FilterDropdown(
    currentMode: ExploreFilterMode,
    onModeChanged: (ExploreFilterMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF1F5F9)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sort by: ${currentMode.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF64748B))
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            ExploreFilterMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onModeChanged(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ModernMerchantCard(
    business: Business,
    couponDiscount: Int?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = business.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )
                
                if (couponDiscount != null) {
                    Surface(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart),
                        color = Color(0xFFF97316),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "-$couponDiscount%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
                
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFACC15), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("4.8", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
            
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            business.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            "${business.category} · ${business.distanceKm}km away",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF1F5F9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF0F172A))
                    }
                }
            }
        }
    }
}
