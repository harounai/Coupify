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

    override suspend fun acceptNotification(token: String, notificationId: String): Any = Any()

    override suspend fun declineNotification(token: String, notificationId: String): Any = Any()
}
