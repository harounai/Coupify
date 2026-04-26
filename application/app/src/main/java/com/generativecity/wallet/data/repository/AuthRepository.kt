package com.generativecity.wallet.data.repository

import com.generativecity.wallet.data.local.RewardInventoryEntity
import com.generativecity.wallet.data.local.UserEntity
import com.generativecity.wallet.data.local.WalletDao
import com.generativecity.wallet.data.model.UserRole
import com.generativecity.wallet.data.remote.*
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class AuthRepository(
    private val walletDao: WalletDao,
    private val apiService: BackendApiService
) {
    fun observeCurrentUser(): Flow<UserEntity?> = walletDao.observeLatestUser()

    suspend fun login(email: String, password: String): Result<UserEntity> {
        return try {
            val response = apiService.login(LoginRequestDto(email, password))
            val role = if ((response.user.role ?: "USER").equals("COMPANY", ignoreCase = true)) UserRole.COMPANY else UserRole.USER
            val user = UserEntity(
                id = response.user.id,
                role = role,
                username = response.user.display_name,
                email = email,
                token = response.access_token,
                hasCompletedOnboarding = response.user.has_completed_onboarding,
                interestsCsv = response.user.interests.joinToString(","),
                explorationPreference = response.user.exploration_preference,
                companyName = response.user.business_id,
                companyCategory = null,
                maxDiscountPercent = null
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
            val response = apiService.register(RegisterRequestDto(email, password, displayName, role = "USER"))
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
        } catch (e: HttpException) {
            val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            Result.failure(Exception("Register failed (${e.code()}): ${body ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerCompany(email: String, password: String, displayName: String, businessId: String): Result<UserEntity> {
        return try {
            val response = apiService.register(
                RegisterRequestDto(email, password, displayName, role = "COMPANY", business_id = businessId)
            )
            val user = UserEntity(
                id = response.user.id,
                role = UserRole.COMPANY,
                username = displayName,
                email = email,
                token = response.access_token,
                hasCompletedOnboarding = true,
                companyName = businessId
            )
            walletDao.upsertUser(user)
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
        // Best-effort backend logout (revokes current session) then clear local state.
        val token = walletDao.getLatestUser()?.token
        if (!token.isNullOrBlank()) {
            runCatching { apiService.logout("Bearer $token") }
        }
        walletDao.clearUsers()
        walletDao.clearOffers()
        walletDao.clearInventory()
    }

    suspend fun enterMerchantMode(businessId: String): Result<UserEntity> {
        return try {
            val id = businessId.ifBlank { "biz_coffee_1" }
            val user = UserEntity(
                id = id,
                role = UserRole.COMPANY,
                username = "Merchant",
                email = "merchant@local",
                token = null,
                hasCompletedOnboarding = true,
                companyName = id,
                companyCategory = "merchant",
                maxDiscountPercent = 20
            )
            walletDao.upsertUser(user)
            Result.success(user)
        } catch (e: HttpException) {
            val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            Result.failure(Exception("Company register failed (${e.code()}): ${body ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
