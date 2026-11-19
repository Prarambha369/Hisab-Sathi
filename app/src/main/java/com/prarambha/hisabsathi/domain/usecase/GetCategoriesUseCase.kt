package com.prarambha.hisabsathi.domain.usecase

import com.prarambha.hisabsathi.data.database.entity.CategoryEntity
import com.prarambha.hisabsathi.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    fun execute(): Flow<List<CategoryEntity>> {
        return categoryRepository.getAllCategories()
    }
}