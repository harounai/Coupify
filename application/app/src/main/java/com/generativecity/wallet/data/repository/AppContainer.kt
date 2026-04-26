package com.generativecity.wallet.data.repository

import android.content.Context
import com.generativecity.wallet.data.local.AppDatabase
import com.generativecity.wallet.data.remote.OfferApiService
import com.generativecity.wallet.domain.usecase.GeneratePersonalizedOfferUseCase
import com.generativecity.wallet.utils.NotificationHelper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val walletDao = database.walletDao()
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val offerApi = retrofit.create(OfferApiService::class.java)
    
    val notificationHelper = NotificationHelper(context)

    val authRepository = AuthRepository(walletDao)
    val offerRepository = OfferRepository(walletDao, offerApi)
    val rewardRepository = RewardRepository(walletDao)

    val generatePersonalizedOfferUseCase = GeneratePersonalizedOfferUseCase(offerRepository)
}
