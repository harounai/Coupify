package com.generativecity.wallet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward_inventory")
data class RewardInventoryEntity(
    @PrimaryKey val userId: Int,
    val coins: Int = 3,
    val boosts: Int = 1,
    val streakDays: Int = 1,
    val lastLoginEpochMillis: Long = System.currentTimeMillis(),
    val streakFreezerCount: Int = 0,
    val doubleOrNothingCount: Int = 0,
    val freeCouponCount: Int = 0,
    val timeExtensionCount: Int = 0
)
