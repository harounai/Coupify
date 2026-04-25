package com.generativecity.wallet.data.remote

class MockWeatherApiService : WeatherApiService {
    override suspend fun getWeather(): WeatherResponse = MockData.sessionWeather
}

class MockOfferApiService : OfferApiService {
    override suspend fun getMerchantDemand(): MerchantDemandResponse {
        val demandMap = MockData.businesses.associate { it.id to it.demandLevel }
        return MerchantDemandResponse(demandByBusinessId = demandMap)
    }
}
