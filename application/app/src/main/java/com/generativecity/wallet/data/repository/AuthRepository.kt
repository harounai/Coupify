package com.generativecity.wallet.data.repository

import com.generativecity.wallet.data.local.RewardInventoryEntity
import com.generativecity.wallet.data.local.UserEntity
import com.generativecity.wallet.data.local.WalletDao
import com.generativecity.wallet.data.model.UserRole
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val walletDao: WalletDao
) {
    fun observeCurrentUser(): Flow<UserEntity?> = walletDao.observeLatestUser()

    suspend fun loginUser(username: String, interests: List<String>, explorationPreference: Int): Int {
        val user = UserEntity(
            role = UserRole.USER,
            username = username,
            interestsCsv = interests.joinToString(","),
            explorationPreference = explorationPreference
        )
        val id = walletDao.upsertUser(user).toInt()
        walletDao.upsertInventory(RewardInventoryEntity(userId = id))
        return id
    }

    suspend fun loginCompany(
        username: String,
        businessName: String,
        category: String,
        maxDiscountPercent: Int
    ): Int {
        val company = UserEntity(
            role = UserRole.COMPANY,
            username = username,
            interestsCsv = category,
            explorationPreference = 0,
            companyName = businessName,
            companyCategory = category,
            maxDiscountPercent = maxDiscountPercent
        )
        val id = walletDao.upsertUser(company).toInt()
        walletDao.upsertInventory(RewardInventoryEntity(userId = id))
        return id
    }

    suspend fun logout() {
        walletDao.clearUsers()
        walletDao.clearOffers()
        walletDao.clearInventory()
    }
}
