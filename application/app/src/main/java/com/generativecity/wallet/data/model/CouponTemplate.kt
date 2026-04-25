package com.generativecity.wallet.data.model

data class CouponTemplate(
    val id: String,
    val title: String,
    val baseDiscount: Int,
    val category: String,
    val durationHours: Int
)
