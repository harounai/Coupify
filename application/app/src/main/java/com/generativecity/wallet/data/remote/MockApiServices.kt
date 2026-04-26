package com.generativecity.wallet.data.remote

class MockWeatherApiService : WeatherApiService {
    override suspend fun getWeather(): WeatherResponse = MockData.sessionWeather
}

class MockOfferApiService : OfferApiService {
    override suspend fun generateOffer(payload: GenerateOfferRequest): GeneratedOfferApiResponse {
        val business = MockData.businesses.first()
        return GeneratedOfferApiResponse(
            offer_id = "mock-offer-id",
            merchant_id = business.id,
            merchant_name = business.name,
            title = "Mock dynamic offer",
            body = "Generated from mock service",
            discount_percent = 15,
            expires_at_epoch_ms = System.currentTimeMillis() + 15 * 60 * 1000,
            qr_payload_seed = "mock-seed",
            widget = WidgetApiResponse(
                theme = "warm-minimal",
                emotion = "cozy",
                badge = "2 min away",
                cta_text = "Warm up now",
                image_prompt = "Coffee shop"
            ),
            context = ContextStateApiResponse(
                context_id = "mock-context",
                city = "stuttgart",
                composite_state = "cloudy+tuesday_12h+browsing+low_density",
                trigger_reason = "mock trigger",
                visible_signals = mapOf("weather" to "cloudy 11C")
            )
        )
    }

    override suspend fun createRedemption(payload: RedemptionCreateRequest): RedemptionCreateResponse {
        return RedemptionCreateResponse(redemption_token = "mock-token", qr_payload = "CITYWALLET::mock-token")
    }

    override suspend fun validateRedemption(payload: RedemptionValidateRequest): RedemptionValidateResponse {
        return RedemptionValidateResponse(valid = true, status = "redeemed", cashback_eur = 1.5, redeemed_at_epoch_ms = System.currentTimeMillis())
    }

    override suspend fun getMerchantDashboard(): MerchantDashboardResponse {
        return MerchantDashboardResponse(
            city = "stuttgart",
            generated_total = 2,
            accepted_total = 1,
            redeemed_total = 1,
            merchants = emptyList()
        )
    }
}
