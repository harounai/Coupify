package com.generativecity.wallet.data.repository

import android.content.Context
import com.generativecity.wallet.data.local.AppDatabase
import com.generativecity.wallet.data.remote.BackendApiService
import com.generativecity.wallet.data.remote.MockOfferApiService
import com.generativecity.wallet.data.remote.MockWeatherApiService
import com.generativecity.wallet.domain.usecase.ContextEngine
import com.generativecity.wallet.domain.usecase.GeneratePersonalizedOfferUseCase
import com.generativecity.wallet.utils.NotificationHelper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val walletDao = database.walletDao()

    private val weatherApi = MockWeatherApiService()
    private val offerApi = MockOfferApiService()
    private val contextEngine = ContextEngine()
    
    val notificationHelper = NotificationHelper(context)
    val offerRepository = OfferRepository(walletDao, weatherApi, offerApi, contextEngine)
    val rewardRepository = RewardRepository(walletDao)

    val generatePersonalizedOfferUseCase = GeneratePersonalizedOfferUseCase(offerRepository)

    // Backend (local): Android emulator reaches host via 10.0.2.2
    private val okHttp = OkHttpClient.Builder().build()
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8000/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val backendApi: BackendApiService = retrofit.create(BackendApiService::class.java)

    val authRepository = AuthRepository(walletDao, backendApi)
    val homeRepository = HomeRepository(backendApi, walletDao)
    val notificationsRepository = NotificationsRepository(backendApi, walletDao)
    val merchantRepository = MerchantRepository(backendApi)
}
