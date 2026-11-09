package com.prarambha.hisabsathi.domain.usecase

import android.util.Log
import com.prarambha.hisabsathi.data.database.entity.SubscriptionEntity
import com.prarambha.hisabsathi.data.repository.SubscriptionRepository
import javax.inject.Inject

class UpdateSubscriptionUseCase
@Inject
constructor(private val subscriptionRepository: SubscriptionRepository) {
    suspend fun execute(subscription: SubscriptionEntity) {
        Log.d("UpdateSubscriptionUseCase", "Updating subscription entity: ${subscription.id}")
        subscriptionRepository.updateSubscription(subscription)
        Log.d("UpdateSubscriptionUseCase", "Subscription updated successfully.")
    }
}
