package com.generativecity.wallet.data.remote

import com.generativecity.wallet.data.model.Business
import com.generativecity.wallet.data.model.CouponTemplate
import kotlin.random.Random

object MockData {
    val businesses = listOf(
        Business(
            id = "biz_coffee_1",
            name = "Nebula Coffee Lab",
            category = "coffee",
            distanceKm = 0.7,
            demandLevel = 85,
            imageUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085"
        ),
        Business(
            id = "biz_gym_1",
            name = "Pulse Forge Gym",
            category = "fitness",
            distanceKm = 1.8,
            demandLevel = 60,
            imageUrl = "https://images.unsplash.com/photo-1517836357463-d25dfeac3438"
        ),
        Business(
            id = "biz_restaurant_1",
            name = "Urban Harvest Kitchen",
            category = "food",
            distanceKm = 1.2,
            demandLevel = 75,
            imageUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5"
        ),
        Business(
            id = "biz_coffee_2",
            name = "Roast Republic",
            category = "coffee",
            distanceKm = 2.4,
            demandLevel = 68,
            imageUrl = "https://images.unsplash.com/photo-1442512595331-e89e73853f31"
        ),
        Business(
            id = "biz_fitness_2",
            name = "Skyline Mobility Studio",
            category = "fitness",
            distanceKm = 3.1,
            demandLevel = 71,
            imageUrl = "https://images.unsplash.com/photo-1518611012118-696072aa579a"
        ),
        Business(
            id = "biz_food_2",
            name = "Metro Bento House",
            category = "food",
            distanceKm = 0.9,
            demandLevel = 90,
            imageUrl = "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe"
        )
    )

    val coupons = listOf(
        CouponTemplate(
            id = "coupon_coffee_1",
            title = "Coffee Power Hour",
            baseDiscount = 15,
            category = "coffee",
            durationHours = 3
        ),
        CouponTemplate(
            id = "coupon_fitness_1",
            title = "Fitness Trial Sprint",
            baseDiscount = 20,
            category = "fitness",
            durationHours = 4
        ),
        CouponTemplate(
            id = "coupon_food_1",
            title = "Lunch Break Deal",
            baseDiscount = 18,
            category = "food",
            durationHours = 2
        ),
        CouponTemplate(
            id = "coupon_coffee_2",
            title = "Cold Brew Express",
            baseDiscount = 22,
            category = "coffee",
            durationHours = 2
        ),
        CouponTemplate(
            id = "coupon_fitness_2",
            title = "After Work Burn",
            baseDiscount = 16,
            category = "fitness",
            durationHours = 5
        ),
        CouponTemplate(
            id = "coupon_food_2",
            title = "Chef Spotlight Menu",
            baseDiscount = 24,
            category = "food",
            durationHours = 3
        )
    )

    // Fixed once per app process to simulate weather staying stable during a session.
    val sessionWeather: WeatherResponse by lazy {
        val weatherOptions = listOf(
            WeatherResponse("Sunny", 27),
            WeatherResponse("Cloudy", 22),
            WeatherResponse("Rainy", 19)
        )
        weatherOptions[Random(System.currentTimeMillis()).nextInt(weatherOptions.size)]
    }
}
