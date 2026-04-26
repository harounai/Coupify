package com.generativecity.wallet.data.remote

import retrofit2.http.*

// Auth DTOs
data class LoginRequestDto(val email: String, val password: String)
data class RegisterRequestDto(val email: String, val password: String, val display_name: String)
data class AuthResponseDto(val access_token: String, val token_type: String, val user: UserProfileDto)

// Survey/Preferences DTOs
data class UserPreferencesDto(
    val interests: List<String>,
    val budget_range: String, // e.g. "LOW", "MEDIUM", "HIGH"
    val active_times: List<String>, // e.g. "MORNING", "AFTERNOON", "EVENING", "NIGHT"
    val behavior_frequency: String, // e.g. "DAILY", "WEEKLY"
    val is_spontaneous: Boolean,
    val location_habits: List<String>, // e.g. "DOWNTOWN", "SUBURBS"
    val environment_preference: String, // e.g. "INDOOR", "OUTDOOR", "BOTH"
    val discovery_mode: String, // e.g. "POPULAR", "HIDDEN_GEMS"
    val current_mood_context: String? = null
)

// DTOs matching backend/app/schemas.py
data class UserProfileDto(
    val id: String,
    val display_name: String,
    val email: String? = null,
    val interests: List<String> = emptyList(),
    val exploration_preference: Int = 50,
    val has_completed_onboarding: Boolean = false
)

data class StreakStateDto(
    val current_days: Int,
    val best_days: Int,
    val last_checkin_date: String?,
    val reward_unlocked_7: Boolean,
    val reward_unlocked_30: Boolean
)

data class BusinessDto(
    val id: String,
    val name: String,
    val category: String,
    val lat: Double,
    val lon: Double,
    val image_url: String,
    val distance_km: Double? = null,
    val demand_level: Int? = null
)

data class CouponTemplateDto(
    val id: String,
    val title: String,
    val category: String,
    val base_discount: Int,
    val duration_hours: Int
)

data class CouponInstanceDto(
    val id: String,
    val status: String,
    val discount_percent: Int,
    val created_at: String,
    val expires_at: String,
    val redeemed_at: String?,
    val day_key: String,
    val business: BusinessDto,
    val template: CouponTemplateDto
)

data class HomeFeedDto(
    val user: UserProfileDto,
    val streak: StreakStateDto,
    val live_opportunities: List<CouponInstanceDto>,
    val claimed_rewards_today: List<CouponInstanceDto>,
    val offer_of_the_day: CouponInstanceDto?,
    val new_in_town: List<BusinessDto>
)

data class UpdateUserProfileRequestDto(
    val display_name: String? = null,
    val interests: List<String>? = null,
    val exploration_preference: Int? = null,
    val has_completed_onboarding: Boolean? = null
)

data class ClaimCouponRequestDto(
    val template_id: String,
    val business_id: String
)

data class RedeemCouponResponseDto(
    val coupon: CouponInstanceDto
)

data class NotificationInboxItemDto(
    val id: String,
    val coupon_instance_id: String,
    val business_name: String,
    val image_url: String,
    val discount_percent: Int,
    val title: String,
    val body: String,
    val status: String,
    val created_at: String
)

data class RegisterDeviceRequestDto(
    val fcm_token: String
)

interface BackendApiService {
    // Auth Endpoints
    @POST("v1/auth/register")
    suspend fun register(@Body body: RegisterRequestDto): AuthResponseDto

    @POST("v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @GET("v1/auth/me")
    suspend fun getMe(@Header("Authorization") token: String): UserProfileDto

    // Preferences
    @PUT("v1/user/preferences")
    suspend fun updatePreferences(
        @Header("Authorization") token: String,
        @Body body: UserPreferencesDto
    ): UserProfileDto

    @GET("v1/home")
    suspend fun getHome(
        @Header("Authorization") token: String,
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null
    ): HomeFeedDto

    @PATCH("v1/users/me")
    suspend fun updateMe(
        @Header("Authorization") token: String,
        @Body body: UpdateUserProfileRequestDto
    ): UserProfileDto

    @POST("v1/streaks/checkin")
    suspend fun checkIn(@Header("Authorization") token: String): Any

    @POST("v1/coupons/claim")
    suspend fun claim(
        @Header("Authorization") token: String,
        @Body body: ClaimCouponRequestDto
    ): CouponInstanceDto

    @POST("v1/coupons/{couponId}/redeem")
    suspend fun redeem(
        @Header("Authorization") token: String,
        @Path("couponId") couponId: String
    ): RedeemCouponResponseDto

    @GET("v1/notifications/inbox")
    suspend fun getNotificationInbox(@Header("Authorization") token: String): List<NotificationInboxItemDto>

    @POST("v1/notifications/{notificationId}/accept")
    suspend fun acceptNotification(
        @Header("Authorization") token: String,
        @Path("notificationId") notificationId: String
    ): Any

    @POST("v1/notifications/{notificationId}/decline")
    suspend fun declineNotification(
        @Header("Authorization") token: String,
        @Path("notificationId") notificationId: String
    ): Any

    @POST("v1/notifications/register-device")
    suspend fun registerDevice(
        @Header("Authorization") token: String,
        @Body body: RegisterDeviceRequestDto
    ): Any
}
