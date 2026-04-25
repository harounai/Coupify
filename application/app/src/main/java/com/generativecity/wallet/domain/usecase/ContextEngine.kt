package com.generativecity.wallet.domain.usecase

import com.generativecity.wallet.data.model.Business
import com.generativecity.wallet.data.model.ContextData
import com.generativecity.wallet.data.model.CouponTemplate
import com.generativecity.wallet.data.model.GeneratedOffer
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class ContextEngine {
    fun generateOffer(
        context: ContextData,
        businesses: List<Business>,
        coupons: List<CouponTemplate>
    ): GeneratedOffer {
        // 70/30 exploration strategy:
        // Most of the time we prefer categories matching user interests,
        // while still introducing occasional discovery offers.
        val weightedBusinesses = businesses.sortedByDescending { business ->
            val preferenceBoost = if (context.preferences.contains(business.category)) 70 else 30
            val demandBoost = context.merchantDemandMap[business.id] ?: 50
            val distancePenalty = max(0, 20 - (business.distanceKm * 10).toInt())
            preferenceBoost + demandBoost + distancePenalty
        }

        val shouldExplore = Random.nextInt(100) >= 70
        val selectedBusiness = if (shouldExplore) {
            weightedBusinesses.shuffled().first()
        } else {
            weightedBusinesses.first()
        }

        val coupon = coupons.firstOrNull { it.category == selectedBusiness.category } ?: coupons.first()

        val weatherBonus = if (context.weather.condition == "Rainy" && selectedBusiness.category == "food") 5 else 0
        val timeBonus = if (context.hourOfDay in 7..10 && selectedBusiness.category == "coffee") 6 else 0
        val explorationBonus = (context.explorationPreference / 20)

        val discount = min(55, coupon.baseDiscount + weatherBonus + timeBonus + explorationBonus)
        val expiryMillis = System.currentTimeMillis() + (coupon.durationHours * 60 * 60 * 1000)

        return GeneratedOffer(
            title = "${coupon.title} @ ${selectedBusiness.name}",
            discount = discount,
            expiryEpochMillis = expiryMillis,
            businessId = selectedBusiness.id,
            businessName = selectedBusiness.name,
            distanceKm = selectedBusiness.distanceKm,
            category = selectedBusiness.category,
            imageUrl = selectedBusiness.imageUrl
        )
    }
}
