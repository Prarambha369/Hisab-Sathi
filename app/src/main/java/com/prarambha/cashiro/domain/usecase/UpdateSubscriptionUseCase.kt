package com.prarambha.cashiro.domain.usecase

import com.prarambha.cashiro.BuildConfig

import android.util.Log
import com.prarambha.cashiro.data.database.entity.SubscriptionEntity
import com.prarambha.cashiro.data.repository.SubscriptionRepository
import javax.inject.Inject

class UpdateSubscriptionUseCase
@Inject
constructor(private val subscriptionRepository: SubscriptionRepository) {
    suspend fun execute(subscription: SubscriptionEntity) {
        if (BuildConfig.DEBUG) Log.d("UpdateSubscriptionUseCase", "Updating subscription entity: ${subscription.id}")
        subscriptionRepository.updateSubscription(subscription)
        if (BuildConfig.DEBUG) Log.d("UpdateSubscriptionUseCase", "Subscription updated successfully.")
    }
}
