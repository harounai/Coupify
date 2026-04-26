package com.generativecity.wallet.data.repository

import com.generativecity.wallet.data.remote.BackendApiService
import com.generativecity.wallet.data.remote.ClaimCouponRequestDto
import com.generativecity.wallet.data.remote.CouponInstanceDto
import com.generativecity.wallet.data.remote.HomeFeedDto

class HomeRepository(
    private val api: BackendApiService,
    private val walletDao: com.generativecity.wallet.data.local.WalletDao
) {
    suspend fun fetchHome(lat: Double? = null, lon: Double? = null): HomeFeedDto {
        val token = walletDao.getLatestUser()?.token ?: error("Missing session token")
        return api.getHome(token = "Bearer $token", lat = lat, lon = lon)
    }

    suspend fun claim(templateId: String, businessId: String): CouponInstanceDto {
        val token = walletDao.getLatestUser()?.token ?: error("Missing session token")
        return api.claim(
            token = "Bearer $token",
            body = ClaimCouponRequestDto(template_id = templateId, business_id = businessId)
        )
    }

    suspend fun redeem(couponId: String): CouponInstanceDto {
        val token = walletDao.getLatestUser()?.token ?: error("Missing session token")
        return api.redeem(token = "Bearer $token", couponId = couponId).coupon
    }
}

