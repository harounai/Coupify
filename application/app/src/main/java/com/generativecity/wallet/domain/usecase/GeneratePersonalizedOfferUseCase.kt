package com.generativecity.wallet.domain.usecase

import com.generativecity.wallet.data.model.GeneratedOffer
import com.generativecity.wallet.data.repository.OfferRepository

class GeneratePersonalizedOfferUseCase(
    private val offerRepository: OfferRepository
) {
    suspend operator fun invoke(userId: String): GeneratedOffer {
        return offerRepository.generatePersonalizedOffer(userId)
    }
}
