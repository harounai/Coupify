package com.generativecity.wallet.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OfferApiService {
    @POST("offers/generate")
    suspend fun generateOffer(@Body payload: GenerateOfferRequest): GeneratedOfferApiResponse

    @POST("redemptions/create")
    suspend fun createRedemption(@Body payload: RedemptionCreateRequest): RedemptionCreateResponse

    @POST("redemptions/validate")
    suspend fun validateRedemption(@Body payload: RedemptionValidateRequest): RedemptionValidateResponse

    @GET("merchant/dashboard")
    suspend fun getMerchantDashboard(): MerchantDashboardResponse
}

data class GenerateOfferRequest(
    val user_context: UserContextApi,
    val merchant_rules: List<MerchantRuleApi>,
    val payone_density_by_merchant: Map<String, Int>
)

data class UserContextApi(
    val user_id: Int,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val intent_signal: String,
    val weather_condition: String,
    val temperature_c: Double,
    val event_tags: List<String>,
    val hour_of_day: Int,
    val weekday: String,
    val movement_state: String
)

data class MerchantRuleApi(
    val merchant_id: String,
    val merchant_name: String,
    val category: String,
    val max_discount_percent: Int,
    val goal: String,
    val quiet_hour_multiplier: Double,
    val min_spend_eur: Double
)

data class GeneratedOfferApiResponse(
    val offer_id: String,
    val merchant_id: String,
    val merchant_name: String,
    val title: String,
    val body: String,
    val discount_percent: Int,
    val expires_at_epoch_ms: Long,
    val qr_payload_seed: String,
    val widget: WidgetApiResponse,
    val context: ContextStateApiResponse
)

data class WidgetApiResponse(
    val theme: String,
    val emotion: String,
    val badge: String,
    val cta_text: String,
    val image_prompt: String
)

data class ContextStateApiResponse(
    val context_id: String,
    val city: String,
    val composite_state: String,
    val trigger_reason: String,
    val visible_signals: Map<String, String>
)

data class RedemptionCreateRequest(
    val offer_id: String,
    val user_id: Int,
    val merchant_id: String
)

data class RedemptionCreateResponse(
    val redemption_token: String,
    val qr_payload: String
)

data class RedemptionValidateRequest(
    val redemption_token: String
)

data class RedemptionValidateResponse(
    val valid: Boolean,
    val status: String,
    val cashback_eur: Double,
    val redeemed_at_epoch_ms: Long?
)

data class MerchantDashboardResponse(
    val city: String,
    val generated_total: Int,
    val accepted_total: Int,
    val redeemed_total: Int,
    val merchants: List<MerchantMetricsResponse>
)

data class MerchantMetricsResponse(
    val merchant_id: String,
    val generated_offers: Int,
    val accepted_offers: Int,
    val redeemed_offers: Int,
    val acceptance_rate: Double,
    val redemption_rate: Double
)
