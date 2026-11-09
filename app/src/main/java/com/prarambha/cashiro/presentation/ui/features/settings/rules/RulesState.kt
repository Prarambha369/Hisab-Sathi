package com.prarambha.cashiro.presentation.ui.features.settings.rules

import com.prarambha.cashiro.domain.usecase.BatchApplyResult

data class RulesUiState(
    val isLoading: Boolean = false,
    val batchApplyProgress: Pair<Int, Int>? = null,
    val batchApplyResult: BatchApplyResult? = null
)
