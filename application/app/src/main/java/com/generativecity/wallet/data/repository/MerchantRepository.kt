package com.generativecity.wallet.data.repository

import com.generativecity.wallet.data.remote.BackendApiService
import com.generativecity.wallet.data.remote.MerchantRuleInDto
import com.generativecity.wallet.data.remote.MerchantRuleDto
import com.generativecity.wallet.data.remote.MerchantStatsDto
import com.generativecity.wallet.data.remote.MerchantBusinessDto

class MerchantRepository(
    private val api: BackendApiService
) {
    suspend fun listBusinesses(): List<MerchantBusinessDto> = api.listMerchantBusinesses()

    suspend fun getRules(businessId: String): MerchantRuleDto = api.getMerchantRules(businessId)

    suspend fun saveRules(businessId: String, body: MerchantRuleInDto): MerchantRuleDto =
        api.putMerchantRules(businessId, body)

    suspend fun getStats(businessId: String): MerchantStatsDto = api.getMerchantStats(businessId)

    suspend fun simulateLowDemand(businessId: String, minutes: Int = 60, demandLevel: Int = 20) {
        api.simulateLowDemand(businessId, minutes = minutes, demandLevel = demandLevel)
    }
}

