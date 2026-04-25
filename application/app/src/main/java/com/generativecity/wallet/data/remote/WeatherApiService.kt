package com.generativecity.wallet.data.remote

interface WeatherApiService {
    suspend fun getWeather(): WeatherResponse
}

data class WeatherResponse(
    val condition: String,
    val temperatureC: Int
)
