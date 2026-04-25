package com.generativecity.wallet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.generativecity.wallet.data.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: UserRole,
    val username: String,
    val interestsCsv: String,
    val explorationPreference: Int,
    val companyName: String? = null,
    val companyCategory: String? = null,
    val maxDiscountPercent: Int? = null
)
