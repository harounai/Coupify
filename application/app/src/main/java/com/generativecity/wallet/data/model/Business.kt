package com.generativecity.wallet.data.model

data class Business(
    val id: String,
    val name: String,
    val category: String,
    val distanceKm: Double,
    val demandLevel: Int,
    val imageUrl: String
)
