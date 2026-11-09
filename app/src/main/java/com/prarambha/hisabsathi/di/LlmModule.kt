package com.prarambha.hisabsathi.di

import com.prarambha.hisabsathi.data.service.LlmServiceImpl
import com.prarambha.hisabsathi.domain.service.LlmService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LlmModule {
    
    @Binds
    @Singleton
    abstract fun bindLlmService(
        llmServiceImpl: LlmServiceImpl
    ): LlmService
}