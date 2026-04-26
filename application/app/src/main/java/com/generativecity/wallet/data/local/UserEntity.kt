package com.generativecity.wallet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.generativecity.wallet.data.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // Changed to String to match backend UUID
    val role: UserRole,
    val username: String,
    val email: String,
    val token: String? = null,
    val hasCompletedOnboarding: Boolean = false,
    val interestsCsv: String = "",
    val explorationPreference: Int = 50,
    val companyName: String? = null,
    val companyCategory: String? = null,
    val maxDiscountPercent: Int? = null
)
