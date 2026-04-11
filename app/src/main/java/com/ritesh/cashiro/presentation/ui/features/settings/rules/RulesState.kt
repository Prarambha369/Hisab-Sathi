package com.ritesh.cashiro.presentation.ui.features.settings.rules

import com.ritesh.cashiro.domain.usecase.BatchApplyResult

data class RulesUiState(
    val isLoading: Boolean = false,
    val batchApplyProgress: Pair<Int, Int>? = null,
    val batchApplyResult: BatchApplyResult? = null
)
