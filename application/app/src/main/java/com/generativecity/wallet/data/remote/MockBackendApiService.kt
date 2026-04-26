package com.generativecity.wallet.data.remote

import java.util.UUID

class MockBackendApiService : BackendApiService {
    
    private val mockUser = UserProfileDto(
        id = UUID.randomUUID().toString(),
        display_name = "Test User",
        email = "test@example.com",
        has_completed_onboarding = false
    )

    override suspend fun register(body: RegisterRequestDto): AuthResponseDto {
        return AuthResponseDto(
            access_token = "mock_token",
            token_type = "bearer",
            user = mockUser.copy(display_name = body.display_name)
        )
    }

    override suspend fun login(body: LoginRequestDto): AuthResponseDto {
        return AuthResponseDto(
            access_token = "mock_token",
            token_type = "bearer",
            user = mockUser
        )
    }

    override suspend fun getMe(token: String): UserProfileDto = mockUser

    override suspend fun logout(token: String): Any = Any()

    override suspend fun updatePreferences(token: String, body: UserPreferencesDto): UserProfileDto {
        return mockUser.copy(has_completed_onboarding = true, interests = body.interests)
    }

    override suspend fun getHome(token: String, lat: Double?, lon: Double?): HomeFeedDto {
        // Return dummy data or bridge to MockData
        return HomeFeedDto(
            user = mockUser,
            streak = StreakStateDto(7, 10, null, true, false),
            live_opportunities = emptyList(),
            claimed_rewards_today = emptyList(),
            offer_of_the_day = null,
            new_in_town = emptyList()
        )
    }

    override suspend fun updateMe(token: String, body: UpdateUserProfileRequestDto): UserProfileDto = mockUser

    override suspend fun checkIn(token: String): Any = Any()

    override suspend fun claim(token: String, body: ClaimCouponRequestDto): CouponInstanceDto {
        throw NotImplementedError()
    }

    override suspend fun redeem(token: String, couponId: String): RedeemCouponResponseDto {
        throw NotImplementedError()
    }

    override suspend fun getNotificationInbox(token: String): List<NotificationInboxItemDto> = emptyList()

    override suspend fun registerDevice(token: String, body: RegisterDeviceRequestDto): Any = Any()

    override suspend fun acceptNotification(token: String, notificationId: String): Any = Any()

    override suspend fun declineNotification(token: String, notificationId: String): Any = Any()

    // Merchant endpoints (stubs for offline / preview builds)
    override suspend fun listMerchantBusinesses(): List<MerchantBusinessDto> {
        return MockData.businesses.map {
            MerchantBusinessDto(
                id = it.id,
                name = it.name,
                category = it.category,
                lat = 0.0,
                lon = 0.0,
                image_url = it.imageUrl
            )
        }
    }

    override suspend fun getMerchantRules(businessId: String): MerchantRuleDto {
        return MerchantRuleDto(
            business_id = businessId,
            max_discount_percent = 20,
            min_discount_percent = 5,
            quiet_hours_start = null,
            quiet_hours_end = null,
            goal = "FILL_QUIET_HOURS",
            coupons_per_day = 50,
            coupons_total = 1000,
            coupons_total_issued = 0,
            products = listOf("coffee", "food"),
            rules_json = emptyMap(),
            updated_at = "2026-01-01T00:00:00Z"
        )
    }

    override suspend fun putMerchantRules(businessId: String, body: MerchantRuleInDto): MerchantRuleDto {
        return MerchantRuleDto(
            business_id = businessId,
            max_discount_percent = body.max_discount_percent,
            min_discount_percent = body.min_discount_percent,
            quiet_hours_start = body.quiet_hours_start,
            quiet_hours_end = body.quiet_hours_end,
            goal = body.goal,
            coupons_per_day = body.coupons_per_day,
            coupons_total = body.coupons_total,
            coupons_total_issued = 0,
            products = body.products,
            rules_json = body.rules_json,
            updated_at = "2026-01-01T00:00:00Z"
        )
    }

    override suspend fun getMerchantStats(businessId: String, dayKey: String?): MerchantStatsDto {
        return MerchantStatsDto(
            business_id = businessId,
            day_key = dayKey ?: "2026-01-01",
            impressions = 0,
            accepts = 0,
            declines = 0,
            redemptions = 0
        )
    }

    override suspend fun simulateLowDemand(businessId: String, minutes: Int, demandLevel: Int): Map<String, Any?> {
        return mapOf("business_id" to businessId, "minutes" to minutes, "demand_level" to demandLevel)
    }
}
