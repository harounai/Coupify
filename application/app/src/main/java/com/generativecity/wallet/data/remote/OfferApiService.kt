package com.generativecity.wallet.data.remote

interface OfferApiService {
    suspend fun getMerchantDemand(): MerchantDemandResponse
}

data class MerchantDemandResponse(
    val demandByBusinessId: Map<String, Int>
)
