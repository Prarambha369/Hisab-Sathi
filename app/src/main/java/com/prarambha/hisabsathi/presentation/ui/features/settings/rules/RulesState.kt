package com.prarambha.hisabsathi.presentation.ui.features.settings.rules

import com.prarambha.hisabsathi.domain.usecase.BatchApplyResult

data class RulesUiState(
    val isLoading: Boolean = false,
    val batchApplyProgress: Pair<Int, Int>? = null,
    val batchApplyResult: BatchApplyResult? = null
)
