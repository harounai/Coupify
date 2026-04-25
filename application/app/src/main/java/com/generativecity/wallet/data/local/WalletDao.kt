package com.generativecity.wallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity): Long

    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
    fun observeLatestUser(): Flow<UserEntity?>

    @Query("DELETE FROM users")
    suspend fun clearUsers()

    @Query("DELETE FROM offers")
    suspend fun clearOffers()

    @Query("DELETE FROM reward_inventory")
    suspend fun clearInventory()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<OfferEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOffer(offer: OfferEntity)

    @Update
    suspend fun updateOffer(offer: OfferEntity)

    @Query("SELECT * FROM offers WHERE userId = :userId")
    fun observeOffersForUser(userId: Int): Flow<List<OfferEntity>>

    @Query("SELECT * FROM offers WHERE id = :offerId LIMIT 1")
    suspend fun getOfferById(offerId: String): OfferEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInventory(inventory: RewardInventoryEntity)

    @Query("SELECT * FROM reward_inventory WHERE userId = :userId LIMIT 1")
    fun observeInventory(userId: Int): Flow<RewardInventoryEntity?>

    @Query("SELECT * FROM reward_inventory WHERE userId = :userId LIMIT 1")
    suspend fun getInventoryByUserId(userId: Int): RewardInventoryEntity?
}
