package com.generativecity.wallet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.generativecity.wallet.data.model.OfferStatus

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey val id: String,
    val userId: Int,
    val title: String,
    val discountPercent: Int,
    val distanceKm: Double,
    val createdEpochMillis: Long = System.currentTimeMillis(),
    val expiryEpochMillis: Long,
    val businessName: String,
    val category: String,
    val imageUrl: String = "",
    val status: OfferStatus
)
