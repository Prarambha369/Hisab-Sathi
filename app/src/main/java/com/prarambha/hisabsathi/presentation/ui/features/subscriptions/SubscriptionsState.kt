package com.prarambha.hisabsathi.presentation.ui.features.subscriptions

import com.prarambha.hisabsathi.data.database.entity.SubscriptionEntity
import java.math.BigDecimal

data class SubscriptionsUiState(
    val activeSubscriptions: List<SubscriptionEntity> = emptyList(),
    val totalMonthlyAmount: BigDecimal = BigDecimal.ZERO,
    val totalYearlyAmount: BigDecimal = BigDecimal.ZERO,
    val targetCurrency: String = "NPR",
    val isLoading: Boolean = true,
    val lastHiddenSubscription: SubscriptionEntity? = null,
    val selectedSubscription: SubscriptionEntity? = null,
    val convertedAmounts: Map<Long, BigDecimal> = emptyMap()
)
