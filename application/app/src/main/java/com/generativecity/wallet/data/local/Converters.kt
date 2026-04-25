package com.generativecity.wallet.data.local

import androidx.room.TypeConverter
import com.generativecity.wallet.data.model.OfferStatus
import com.generativecity.wallet.data.model.UserRole

class Converters {
    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toOfferStatus(value: String): OfferStatus = OfferStatus.valueOf(value)

    @TypeConverter
    fun fromOfferStatus(value: OfferStatus): String = value.name
}
