package com.generativecity.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.generativecity.wallet.data.repository.AppContainer

class AppViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(container.authRepository) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(container.homeRepository) as T
            modelClass.isAssignableFrom(WalletViewModel::class.java) -> WalletViewModel(
                container.offerRepository,
                container.generatePersonalizedOfferUseCase
            ) as T
            modelClass.isAssignableFrom(ExploreViewModel::class.java) -> ExploreViewModel(container.offerRepository) as T
            modelClass.isAssignableFrom(RouletteViewModel::class.java) -> RouletteViewModel(container.rewardRepository) as T
            modelClass.isAssignableFrom(NotificationsViewModel::class.java) -> NotificationsViewModel(
                container.notificationsRepository,
                container.notificationHelper
            ) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(container.rewardRepository) as T
            modelClass.isAssignableFrom(CompanyDashboardViewModel::class.java) -> CompanyDashboardViewModel(container.offerRepository) as T
            modelClass.isAssignableFrom(MerchantDashboardViewModel::class.java) -> MerchantDashboardViewModel(container.merchantRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
