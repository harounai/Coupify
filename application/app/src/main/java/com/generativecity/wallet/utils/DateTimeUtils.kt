package com.generativecity.wallet.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    fun formatExpiry(epochMillis: Long): String = timeFormat.format(Date(epochMillis))

    fun buildQrPayload(userId: Int, offerId: String): String {
        return "user_id=$userId;offer_id=$offerId;timestamp=${System.currentTimeMillis()}"
    }
}
