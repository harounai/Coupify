package com.generativecity.wallet.data.model

data class GeneratedOffer(
    val title: String,
    val discount: Int,
    val expiryEpochMillis: Long,
    val businessId: String,
    val businessName: String,
    val distanceKm: Double,
    val category: String,
    val imageUrl: String
)
