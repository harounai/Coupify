package com.generativecity.wallet.data.repository

import com.generativecity.wallet.data.local.OfferEntity
import com.generativecity.wallet.data.local.UserEntity
import com.generativecity.wallet.data.local.WalletDao
import com.generativecity.wallet.data.model.ContextData
import com.generativecity.wallet.data.model.GeneratedOffer
import com.generativecity.wallet.data.model.OfferStatus
import com.generativecity.wallet.data.model.WeatherInfo
import com.generativecity.wallet.data.remote.MockData
import com.generativecity.wallet.data.remote.OfferApiService
import com.generativecity.wallet.data.remote.WeatherApiService
import com.generativecity.wallet.domain.usecase.ContextEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class OfferRepository(
    private val walletDao: WalletDao,
    private val weatherApiService: WeatherApiService,
    private val offerApiService: OfferApiService,
    private val contextEngine: ContextEngine
) {
    fun observeOffers(userId: String): Flow<List<OfferEntity>> = walletDao.observeOffersForUser(userId)

    suspend fun generatePersonalizedOffer(userId: String): GeneratedOffer {
        val user = walletDao.observeLatestUser().first() ?: error("User must exist before generating offers")
        val weather = weatherApiService.getWeather()
        val demand = offerApiService.getMerchantDemand()
        val preferences = if (user.interestsCsv.isBlank()) emptyList() else user.interestsCsv.split(",")

        val context = ContextData(
            weather = WeatherInfo(weather.condition, weather.temperatureC),
            hourOfDay = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
            locationTag = "Downtown",
            preferences = preferences,
            explorationPreference = user.explorationPreference,
            merchantDemandMap = demand.demandByBusinessId
        )

        return contextEngine.generateOffer(
            context = context,
            businesses = MockData.businesses,
            coupons = MockData.coupons
        )
    }

    suspend fun persistGeneratedOffer(userId: String, offer: GeneratedOffer): String {
        val id = UUID.randomUUID().toString()
        walletDao.upsertOffer(
            OfferEntity(
                id = id,
                userId = userId,
                title = offer.title,
                discountPercent = offer.discount,
                distanceKm = offer.distanceKm,
                createdEpochMillis = System.currentTimeMillis(),
                expiryEpochMillis = offer.expiryEpochMillis,
                businessName = offer.businessName,
                category = offer.category,
                imageUrl = offer.imageUrl,
                status = OfferStatus.ACTIVE
            )
        )
        return id
    }

    suspend fun saveOfferFromMerchant(userId: String, businessId: String) {
        val business = MockData.businesses.find { it.id == businessId } ?: return
        val coupon = MockData.coupons.find { it.category == business.category } ?: MockData.coupons.first()
        
        walletDao.upsertOffer(
            OfferEntity(
                id = businessId, // For manual saves, use businessId as key or UUID
                userId = userId,
                title = "${coupon.title} @ ${business.name}",
                discountPercent = coupon.baseDiscount,
                distanceKm = business.distanceKm,
                createdEpochMillis = System.currentTimeMillis(),
                expiryEpochMillis = System.currentTimeMillis() + (coupon.durationHours * 60 * 60 * 1000),
                businessName = business.name,
                category = business.category,
                imageUrl = business.imageUrl,
                status = OfferStatus.SAVED
            )
        )
    }

    suspend fun updateOfferStatus(offerId: String, newStatus: OfferStatus) {
        val existing = walletDao.getOfferById(offerId) ?: return
        walletDao.updateOffer(existing.copy(status = newStatus))
    }

    suspend fun updateOffer(offer: OfferEntity) {
        walletDao.updateOffer(offer)
    }

    suspend fun createOfferFromCompany(userId: String, user: UserEntity): String {
        val category = user.companyCategory ?: "food"
        val businessName = user.companyName ?: "Merchant"
        val maxDiscount = user.maxDiscountPercent ?: 20
        val id = UUID.randomUUID().toString()
        walletDao.upsertOffer(
            OfferEntity(
                id = id,
                userId = userId,
                title = "AI Suggestion: Boost afternoon $category traffic",
                discountPercent = maxDiscount,
                distanceKm = 0.0,
                createdEpochMillis = System.currentTimeMillis(),
                expiryEpochMillis = System.currentTimeMillis() + 6 * 60 * 60 * 1000,
                businessName = businessName,
                category = category,
                status = OfferStatus.ACTIVE
            )
        )
        return id
    }
}
