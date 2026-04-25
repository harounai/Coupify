package com.generativecity.wallet.data.model

data class ContextData(
    val weather: WeatherInfo,
    val hourOfDay: Int,
    val locationTag: String,
    val preferences: List<String>,
    val explorationPreference: Int,
    val merchantDemandMap: Map<String, Int>
)
