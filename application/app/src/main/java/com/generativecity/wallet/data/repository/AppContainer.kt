package com.generativecity.wallet.data.repository

import android.content.Context
import com.generativecity.wallet.data.local.AppDatabase
import com.generativecity.wallet.data.remote.MockOfferApiService
import com.generativecity.wallet.data.remote.MockWeatherApiService
import com.generativecity.wallet.domain.usecase.ContextEngine
import com.generativecity.wallet.domain.usecase.GeneratePersonalizedOfferUseCase
import com.generativecity.wallet.utils.NotificationHelper

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val walletDao = database.walletDao()

    private val weatherApi = MockWeatherApiService()
    private val offerApi = MockOfferApiService()
    private val contextEngine = ContextEngine()
    
    val notificationHelper = NotificationHelper(context)

    val authRepository = AuthRepository(walletDao)
    val offerRepository = OfferRepository(walletDao, weatherApi, offerApi, contextEngine)
    val rewardRepository = RewardRepository(walletDao)

    val generatePersonalizedOfferUseCase = GeneratePersonalizedOfferUseCase(offerRepository)
}
