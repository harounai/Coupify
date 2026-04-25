package com.generativecity.wallet.data.repository

import com.generativecity.wallet.data.local.RewardInventoryEntity
import com.generativecity.wallet.data.local.WalletDao
import kotlinx.coroutines.flow.Flow

class RewardRepository(
    private val walletDao: WalletDao
) {
    fun observeInventory(userId: Int): Flow<RewardInventoryEntity?> = walletDao.observeInventory(userId)

    suspend fun getInventory(userId: Int): RewardInventoryEntity? = walletDao.getInventoryByUserId(userId)

    suspend fun updateInventory(inventory: RewardInventoryEntity) {
        walletDao.upsertInventory(inventory)
    }

    suspend fun markDailyLogin(userId: Int) {
        val current = walletDao.getInventoryByUserId(userId) ?: RewardInventoryEntity(userId = userId)
        val now = System.currentTimeMillis()
        val oneDayMillis = 24L * 60L * 60L * 1000L
        val streak = if (now - current.lastLoginEpochMillis <= oneDayMillis * 2) {
            current.streakDays + 1
        } else {
            1
        }

        val bonusCoins = if (streak % 30 == 0) 10 else 1
        walletDao.upsertInventory(
            current.copy(
                streakDays = streak,
                coins = current.coins + bonusCoins,
                lastLoginEpochMillis = now
            )
        )
    }
}
