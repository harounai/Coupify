package com.generativecity.wallet.data.repository

import com.generativecity.wallet.data.local.RewardInventoryEntity
import com.generativecity.wallet.data.local.UserEntity
import com.generativecity.wallet.data.local.WalletDao
import com.generativecity.wallet.data.model.UserRole
import com.generativecity.wallet.data.remote.*
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val walletDao: WalletDao,
    private val apiService: BackendApiService
) {
    fun observeCurrentUser(): Flow<UserEntity?> = walletDao.observeLatestUser()

    suspend fun login(email: String, password: String): Result<UserEntity> {
        return try {
            val response = apiService.login(LoginRequestDto(email, password))
            val user = UserEntity(
                id = response.user.id,
                role = UserRole.USER,
                username = response.user.display_name,
                email = email,
                token = response.access_token,
                hasCompletedOnboarding = response.user.has_completed_onboarding,
                interestsCsv = response.user.interests.joinToString(","),
                explorationPreference = response.user.exploration_preference
            )
            walletDao.upsertUser(user)
            // Ensure inventory exists
            if (walletDao.getInventoryByUserId(user.id) == null) {
                walletDao.upsertInventory(RewardInventoryEntity(userId = user.id))
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, displayName: String): Result<UserEntity> {
        return try {
            val response = apiService.register(RegisterRequestDto(email, password, displayName))
            val user = UserEntity(
                id = response.user.id,
                role = UserRole.USER,
                username = displayName,
                email = email,
                token = response.access_token,
                hasCompletedOnboarding = false
            )
            walletDao.upsertUser(user)
            walletDao.upsertInventory(RewardInventoryEntity(userId = user.id))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePreferences(token: String, preferences: UserPreferencesDto): Result<Boolean> {
        return try {
            apiService.updatePreferences("Bearer $token", preferences)
            val currentUser = walletDao.getLatestUser()
            if (currentUser != null) {
                walletDao.upsertUser(currentUser.copy(hasCompletedOnboarding = true))
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        walletDao.clearUsers()
        walletDao.clearOffers()
        walletDao.clearInventory()
    }
}
