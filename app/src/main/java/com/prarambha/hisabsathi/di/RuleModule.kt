package com.prarambha.hisabsathi.di

import com.prarambha.hisabsathi.data.repository.RuleRepositoryImpl
import com.prarambha.hisabsathi.domain.repository.RuleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RuleModule {

    @Binds
    @Singleton
    abstract fun bindRuleRepository(
        ruleRepositoryImpl: RuleRepositoryImpl
    ): RuleRepository
}