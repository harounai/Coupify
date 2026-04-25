package com.generativecity.wallet.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.generativecity.wallet.data.model.UserRole
import com.generativecity.wallet.data.remote.MockData
import com.generativecity.wallet.ui.components.QrCodeDialog
import com.generativecity.wallet.ui.screens.*
import com.generativecity.wallet.utils.DateTimeUtils
import com.generativecity.wallet.viewmodel.*

@Composable
fun AppNavGraph(factory: AppViewModelFactory) {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val walletViewModel: WalletViewModel = viewModel(factory = factory)
    val exploreViewModel: ExploreViewModel = viewModel(factory = factory)
    val notificationsViewModel: NotificationsViewModel = viewModel(factory = factory)
    val rouletteViewModel: RouletteViewModel = viewModel(factory = factory)
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
    val companyViewModel: CompanyDashboardViewModel = viewModel(factory = factory)

    val authState by authViewModel.uiState.collectAsState()
    val currentUser = authState.currentUser
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser?.id) {
        currentUser?.let { user ->
            if (user.role == UserRole.USER) {
                walletViewModel.observeOffers(user.id)
                notificationsViewModel.observe(user.id)
                rouletteViewModel.startTimer()
                rouletteViewModel.observeInventory(user.id)
                profileViewModel.observeInventory(user.id)
            } else {
                companyViewModel.observeOffers(user.id)
            }
        }
    }

    LaunchedEffect(Unit) {
        walletViewModel.events.collect { event ->
            if (event == "already_in_wallet") {
                snackbarHostState.showSnackbar("This coupon is already in your wallet!")
            }
        }
    }

    if (!authState.isLoggedIn) {
        LoginScreen(
            onUserLogin = authViewModel::loginUser,
            onCompanyLogin = authViewModel::loginCompany
        )
        return
    }

    if (currentUser?.role == UserRole.COMPANY) {
        val companyState by companyViewModel.uiState.collectAsState()
        CompanyDashboardScreen(
            state = companyState,
            onGenerateSuggestion = {
                currentUser.let { user ->
                    if (user != null) {
                        companyViewModel.generateSuggestion(user.id, user)
                    }
                }
            },
            onLogout = authViewModel::logout
        )
        return
    }

    val walletState by walletViewModel.uiState.collectAsState()
    val exploreState by exploreViewModel.uiState.collectAsState()
    val rouletteState by rouletteViewModel.uiState.collectAsState()
    val notificationsState by notificationsViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: BottomNavItem.Home.route

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .shadow(30.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFFF97316).copy(alpha = 0.3f)),
                shape = RoundedCornerShape(32.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    userBottomTabs.forEach { tab ->
                        val isSelected = currentRoute == tab.route
                        
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .clickable {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val scale by animateFloatAsState(if (isSelected) 1.2f else 1f, label = "")
                            val tint by animateColorAsState(if (isSelected) Color(0xFFF97316) else Color(0xFF94A3B8), label = "")
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isSelected) tab.icon else getOutlinedIcon(tab.route),
                                    contentDescription = tab.label,
                                    tint = tint,
                                    modifier = Modifier.scale(scale).size(26.dp)
                                )
                                AnimatedVisibility(visible = isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(4.dp)
                                            .background(Color(0xFFF97316), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    walletState = walletState,
                    notificationsState = notificationsState,
                    onUseOffer = { offerId ->
                        walletViewModel.openQrForOffer(offerId)
                    },
                    onDeclineNotification = notificationsViewModel::decline,
                    onAcceptNotification = notificationsViewModel::accept,
                    onInstantPayNotification = notificationsViewModel::instantPay,
                    onOpenNotificationCoupon = { navController.navigate("exploreDetail/$it") },
                    onApplyDouble = { offerId -> 
                        currentUser?.let { notificationsViewModel.applyDoubleOrNothing(it.id, offerId) }
                    },
                    onApplyTime = { offerId ->
                        currentUser?.let { notificationsViewModel.applyTimeExtension(it.id, offerId) }
                    }
                )
            }

            composable(BottomNavItem.Explore.route) {
                ExploreScreen(
                    state = exploreState,
                    onCategoryChanged = exploreViewModel::setCategory,
                    onDistanceChanged = exploreViewModel::setDistance,
                    onFilterModeChanged = exploreViewModel::setFilterMode,
                    onLocationChanged = exploreViewModel::setLocation,
                    onOpenBusiness = { businessId ->
                        navController.navigate("exploreDetail/$businessId")
                    }
                )
            }

            composable(
                route = "exploreDetail/{businessId}",
                arguments = listOf(navArgument("businessId") { type = NavType.StringType })
            ) { backStack ->
                val businessId = backStack.arguments?.getString("businessId").orEmpty()
                val business = MockData.businesses.firstOrNull { it.id == businessId }
                if (business != null) {
                    ExploreDetailScreen(
                        business = business,
                        coupon = MockData.coupons.firstOrNull { it.category == business.category },
                        onBack = { navController.popBackStack() },
                        onSaveToWallet = { walletViewModel.saveOffer(it) }
                    )
                }
            }

            composable(BottomNavItem.Gifts.route) {
                RouletteScreen(
                    state = rouletteState,
                    onSpin = {
                        currentUser?.let { rouletteViewModel.spin(it.id) }
                    }
                )
            }

            composable(BottomNavItem.Streak.route) {
                StreakScreen()
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    state = profileState,
                    onLogout = authViewModel::logout
                )
            }
        }

        val selectedOfferId = walletState.selectedOfferIdForQr
        if (selectedOfferId != null && currentUser != null) {
            QrCodeDialog(
                payload = DateTimeUtils.buildQrPayload(currentUser.id, selectedOfferId),
                onDismiss = {
                    walletViewModel.closeQr()
                }
            )
        }
    }
}

private fun getOutlinedIcon(route: String): ImageVector {
    return when(route) {
        BottomNavItem.Home.route -> Icons.Outlined.Home
        BottomNavItem.Explore.route -> Icons.Outlined.Explore
        BottomNavItem.Gifts.route -> Icons.Outlined.Redeem
        BottomNavItem.Streak.route -> Icons.Outlined.Whatshot
        BottomNavItem.Profile.route -> Icons.Outlined.Person
        else -> Icons.Default.Circle
    }
}
