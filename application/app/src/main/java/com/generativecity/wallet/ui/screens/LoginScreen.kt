package com.generativecity.wallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onRegisterCompany: (String, String, String, String) -> Unit,
    onEnterMerchantMode: (String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    onClearError: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isCompany by remember { mutableStateOf(false) }
    var companyBusinessId by remember { mutableStateOf("biz_coffee_1") }
    var showMerchantDialog by remember { mutableStateOf(false) }
    var merchantBusinessId by remember { mutableStateOf("biz_coffee_1") }
    // NOTE: The repo currently does not include `coupify.png` in app assets/resources.
    // Until you add the file under `application/app/src/main/assets/coupify.png`,
    // we show a clean icon fallback so the header isn't blank.

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
            Spacer(modifier = Modifier.height(64.dp))
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFEDD5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalOffer,
                    contentDescription = "Coupify",
                    tint = Color(0xFFF97316),
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Coupify",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { showMerchantDialog = true }
                    )
                }
            )
            Text(
                if (isLoginMode) "Welcome back!" else "Create your account",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!isLoginMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Register as company", color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                            Switch(checked = isCompany, onCheckedChange = { isCompany = it })
                        }

                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (isCompany) {
                            OutlinedTextField(
                                value = companyBusinessId,
                                onValueChange = { companyBusinessId = it },
                                label = { Text("Business ID (e.g. biz_coffee_1)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (error != null) {
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (isLoginMode) onLogin(email, password)
                            else {
                                if (isCompany) onRegisterCompany(email, password, displayName, companyBusinessId.trim())
                                else onRegister(email, password, displayName)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (isLoginMode) "Login" else "Register", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { 
                isLoginMode = !isLoginMode 
                onClearError()
            }) {
                Text(
                    if (isLoginMode) "Don't have an account? Register" else "Already have an account? Login",
                    color = Color(0xFFF97316)
                )
            }
        }
    }

    if (showMerchantDialog) {
        AlertDialog(
            onDismissRequest = { showMerchantDialog = false },
            title = { Text("Merchant Mode") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Hidden merchant mode for hackathon demos.\nEnter a business id (e.g. biz_coffee_1).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = merchantBusinessId,
                        onValueChange = { merchantBusinessId = it },
                        label = { Text("Business ID") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMerchantDialog = false
                        onEnterMerchantMode(merchantBusinessId.trim())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text("Enter Merchant Mode", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMerchantDialog = false }) { Text("Cancel") }
            }
        )
    }
}
