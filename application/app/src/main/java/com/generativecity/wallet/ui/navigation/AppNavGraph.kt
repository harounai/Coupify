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
import com.generativecity.wallet.data.model.Business as UiBusiness
import com.generativecity.wallet.data.remote.MockData
import com.generativecity.wallet.ui.components.QrCodeDialog
import com.generativecity.wallet.ui.screens.*
import com.generativecity.wallet.utils.DateTimeUtils
import com.generativecity.wallet.viewmodel.*
import kotlinx.coroutines.delay

@Composable
fun AppNavGraph(factory: AppViewModelFactory) {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val walletViewModel: WalletViewModel = viewModel(factory = factory)
    val exploreViewModel: ExploreViewModel = viewModel(factory = factory)
    val notificationsViewModel: NotificationsViewModel = viewModel(factory = factory)
    val rouletteViewModel: RouletteViewModel = viewModel(factory = factory)
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
    val companyViewModel: CompanyDashboardViewModel = viewModel(factory = factory)
    val merchantViewModel: MerchantDashboardViewModel = viewModel(factory = factory)

    val authState by authViewModel.uiState.collectAsState()
    val currentUser = authState.currentUser
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser?.id) {
        currentUser?.let { user ->
            if (user.role == UserRole.USER) {
                homeViewModel.refresh()
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

    // AUTH & ONBOARDING FLOW
    if (!authState.isLoggedIn) {
        LoginScreen(
            onLogin = authViewModel::login,
            onRegister = authViewModel::register,
            onRegisterCompany = authViewModel::registerCompany,
            onEnterMerchantMode = authViewModel::enterMerchantMode,
            isLoading = authState.isLoading,
            error = authState.error,
            onClearError = authViewModel::clearError
        )
        return
    }

    if (!authState.onboardingCompleted && currentUser?.role == UserRole.USER) {
        OnboardingScreen(
            onComplete = authViewModel::completeOnboarding,
            isLoading = authState.isLoading
        )
        return
    }

    if (currentUser?.role == UserRole.COMPANY) {
        val merchantState by merchantViewModel.uiState.collectAsState()
        LaunchedEffect(currentUser?.id) {
            // For company accounts, the backend merchant endpoints are keyed by business_id (biz_*),
            // not by the user id (user_*). We store that business id in UserEntity.companyName.
            val businessId = currentUser?.companyName?.takeIf { it.isNotBlank() } ?: currentUser?.id
            businessId?.let { merchantViewModel.load(it) }
        }
        MerchantDashboardScreen(
            state = merchantState,
            onSaveRules = { maxD, minD, goal, couponsPerDay, couponsTotal, products ->
                merchantViewModel.saveAdvancedRules(maxD, minD, goal, couponsPerDay, couponsTotal, products)
            },
            onSimulateLowDemand = merchantViewModel::simulateLowDemand,
            onLogout = authViewModel::logout
        )
        return
    }

    val walletState by walletViewModel.uiState.collectAsState()
    val homeState by homeViewModel.uiState.collectAsState()
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
                    homeState = homeState,
                    walletState = walletState,
                    notificationsState = notificationsState,
                    onUseOffer = { offerId ->
                        walletViewModel.openQrForOffer(offerId)
                    },
                    onDeclineNotification = notificationsViewModel::decline,
                    onAcceptNotification = notificationsViewModel::accept,
                    onInstantPayNotification = notificationsViewModel::instantPay,
                    onOpenNotificationCoupon = { navController.navigate("exploreDetail/$it") },
                    onClaimFromAi = { coupon -> homeViewModel.claim(coupon) },
                    onPayNowLive = { couponId -> navController.navigate("payNow/$couponId") },
                    onApplyDouble = { offerId -> 
                        currentUser?.let { notificationsViewModel.applyDoubleOrNothing(it.id, offerId) }
                    },
                    onApplyTime = { offerId ->
                        currentUser?.let { notificationsViewModel.applyTimeExtension(it.id, offerId) }
                    }
                )
            }

            composable(
                route = "payNow/{couponId}",
                arguments = listOf(navArgument("couponId") { type = NavType.StringType })
            ) { backStack ->
                val couponId = backStack.arguments?.getString("couponId").orEmpty()
                val coupon = homeState.feed?.live_opportunities?.firstOrNull { it.id == couponId }
                if (coupon == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    PayNowScreen(
                        coupon = coupon,
                        onBack = { navController.popBackStack() },
                        onPaymentSuccess = {
                            homeViewModel.claim(coupon)
                            navController.popBackStack()
                        }
                    )
                }
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

                // Prefer live backend-fed businesses (Home feed), then fall back to mock data.
                // This fixes Home "New in Town" cards opening IDs that don't exist in MockData.
                val backendBiz = homeState.feed?.let { feed ->
                    val fromNewInTown = feed.new_in_town.firstOrNull { it.id == businessId }
                    val fromOfferOfDay = feed.offer_of_the_day?.business?.takeIf { it.id == businessId }
                    val fromLive = feed.live_opportunities.firstOrNull { it.business.id == businessId }?.business
                    val fromClaimed = feed.claimed_rewards_today.firstOrNull { it.business.id == businessId }?.business
                    fromNewInTown ?: fromOfferOfDay ?: fromLive ?: fromClaimed
                }

                val business: UiBusiness? = when {
                    backendBiz != null -> UiBusiness(
                        id = backendBiz.id,
                        name = backendBiz.name,
                        category = backendBiz.category,
                        distanceKm = backendBiz.distance_km ?: 0.0,
                        demandLevel = backendBiz.demand_level ?: 50,
                        imageUrl = backendBiz.image_url
                    )
                    else -> MockData.businesses.firstOrNull { it.id == businessId }
                }

                if (business != null) {
                    ExploreDetailScreen(
                        business = business,
                        coupon = MockData.coupons.firstOrNull { it.category == business.category },
                        onBack = { navController.popBackStack() },
                        onSaveToWallet = { walletViewModel.saveOffer(it) }
                    )
                } else {
                    LaunchedEffect(Unit) { navController.popBackStack() }
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
        val qrActivatedAt = if (selectedOfferId == null) null else walletState.qrActivatedAtEpochMillis[selectedOfferId]

        // Global QR expiry watcher (keeps expiring even if dialog closed)
        LaunchedEffect(walletState.qrActivatedAtEpochMillis) {
            while (true) {
                val now = System.currentTimeMillis()
                val expired = walletState.qrActivatedAtEpochMillis
                    .filterValues { startedAt -> (startedAt + 15 * 60 * 1000L) <= now }
                    .keys
                expired.forEach { offerId ->
                    homeViewModel.redeem(offerId)
                    walletViewModel.redeemOffer(offerId)
                    walletViewModel.clearQrActivation(offerId)
                    if (walletState.selectedOfferIdForQr == offerId) {
                        walletViewModel.closeQr()
                    }
                }
                delay(1_000)
            }
        }

        if (selectedOfferId != null && currentUser != null) {
            if (qrActivatedAt == null) {
                AlertDialog(
                    onDismissRequest = { walletViewModel.closeQr() },
                    title = { Text("Activate QR-code?", fontWeight = FontWeight.Black) },
                    text = {
                        Text("Once activated, this QR-code is valid for 15 minutes. After it expires, the offer will be removed.")
                    },
                    confirmButton = {
                        Button(
                            onClick = { walletViewModel.activateQr(selectedOfferId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                        ) {
                            Text("Activate (15 min)", fontWeight = FontWeight.Black)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { walletViewModel.closeQr() }) {
                            Text("Not now")
                        }
                    }
                )
            } else {
                val remainingSeconds = ((qrActivatedAt + 15 * 60 * 1000L) - System.currentTimeMillis())
                    .coerceAtLeast(0) / 1000
                QrCodeDialog(
                    payload = DateTimeUtils.buildQrPayload(currentUser.id, selectedOfferId),
                    onDismiss = { walletViewModel.closeQr() },
                    remainingSeconds = remainingSeconds
                )
            }
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
