package com.generativecity.wallet.data.repository

import com.generativecity.wallet.data.local.OfferEntity
import com.generativecity.wallet.data.local.UserEntity
import com.generativecity.wallet.data.local.WalletDao
import com.generativecity.wallet.data.model.GeneratedOffer
import com.generativecity.wallet.data.model.OfferStatus
import com.generativecity.wallet.data.remote.GenerateOfferRequest
import com.generativecity.wallet.data.remote.MerchantDashboardResponse
import com.generativecity.wallet.data.remote.MerchantRuleApi
import com.generativecity.wallet.data.remote.MockData
import com.generativecity.wallet.data.remote.OfferApiService
import com.generativecity.wallet.data.remote.RedemptionCreateRequest
import com.generativecity.wallet.data.remote.RedemptionValidateRequest
import com.generativecity.wallet.data.remote.UserContextApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import java.util.Calendar

class OfferRepository(
    private val walletDao: WalletDao,
    private val offerApiService: OfferApiService
) {
    fun observeOffers(userId: Int): Flow<List<OfferEntity>> = walletDao.observeOffersForUser(userId)

    suspend fun generatePersonalizedOffer(userId: Int): GeneratedOffer {
        val user = walletDao.observeLatestUser().first() ?: error("User must exist before generating offers")
        val preferences = if (user.interestsCsv.isBlank()) emptyList() else user.interestsCsv.split(",")
        val businesses = MockData.businesses
        val demandMap = businesses.associate { biz ->
            val adjusted = (biz.demandLevel - (user.explorationPreference / 4)).coerceIn(10, 95)
            biz.id to adjusted
        }
        val now = Calendar.getInstance()
        val weatherCondition = if (now.get(Calendar.MONTH) in listOf(Calendar.NOVEMBER, Calendar.DECEMBER, Calendar.JANUARY)) "overcast" else "cloudy"
        val request = GenerateOfferRequest(
            user_context = UserContextApi(
                user_id = userId,
                city = "stuttgart",
                latitude = 48.7758,
                longitude = 9.1829,
                intent_signal = if ("coffee" in preferences) "warm_drink" else "browse",
                weather_condition = weatherCondition,
                temperature_c = 11.0,
                event_tags = listOf("city-center", "lunch-break"),
                hour_of_day = now.get(Calendar.HOUR_OF_DAY),
                weekday = now.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, java.util.Locale.ENGLISH) ?: "Tuesday",
                movement_state = "browsing"
            ),
            merchant_rules = businesses.map { business ->
                MerchantRuleApi(
                    merchant_id = business.id,
                    merchant_name = business.name,
                    category = business.category,
                    max_discount_percent = 20,
                    goal = "fill_quiet_hours",
                    quiet_hour_multiplier = 1.2,
                    min_spend_eur = 0.0
                )
            },
            payone_density_by_merchant = demandMap
        )
        val response = offerApiService.generateOffer(request)
        val selectedBusiness = businesses.firstOrNull { it.id == response.merchant_id }
        return GeneratedOffer(
            title = response.title,
            discount = response.discount_percent,
            expiryEpochMillis = response.expires_at_epoch_ms,
            businessId = response.offer_id,
            businessName = response.merchant_name,
            distanceKm = selectedBusiness?.distanceKm ?: 0.8,
            category = selectedBusiness?.category ?: "food",
            imageUrl = selectedBusiness?.imageUrl ?: "https://images.unsplash.com/photo-1555396273-367ea4eb4db5"
        )
    }

    suspend fun persistGeneratedOffer(userId: Int, offer: GeneratedOffer): String {
        val id = offer.businessId.ifBlank { UUID.randomUUID().toString() }
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

    suspend fun saveOfferFromMerchant(userId: Int, businessId: String) {
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

    suspend fun createRedemptionPayload(userId: Int, offerId: String): String {
        val offer = walletDao.getOfferById(offerId) ?: return "CITYWALLET::missing-offer"
        val response = offerApiService.createRedemption(
            RedemptionCreateRequest(
                offer_id = offerId,
                user_id = userId,
                merchant_id = offer.businessName.lowercase().replace(" ", "_")
            )
        )
        return response.qr_payload
    }

    suspend fun validateRedemptionByPayload(payload: String): Boolean {
        val token = payload.removePrefix("CITYWALLET::")
        val result = offerApiService.validateRedemption(
            RedemptionValidateRequest(redemption_token = token)
        )
        return result.valid
    }

    suspend fun fetchMerchantDashboard(): MerchantDashboardResponse {
        return offerApiService.getMerchantDashboard()
    }

    suspend fun createOfferFromCompany(userId: Int, user: UserEntity): String {
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
