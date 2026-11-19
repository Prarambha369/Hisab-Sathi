package com.prarambha.hisabsathi.domain.service

import kotlinx.coroutines.flow.Flow

interface LlmService {
    /** [maxTokens] must not exceed the KV-cache size the model file was exported with. */
    suspend fun initialize(modelPath: String, maxTokens: Int): Result<Unit>
    suspend fun generateResponse(prompt: String): Result<String>
    fun generateResponseStream(prompt: String): Flow<String>
    suspend fun reset()
    fun isInitialized(): Boolean
}