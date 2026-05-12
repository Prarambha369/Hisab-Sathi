package com.prarambha.cashiro.domain.usecase

import com.prarambha.cashiro.data.database.entity.CategoryEntity
import com.prarambha.cashiro.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    fun execute(): Flow<List<CategoryEntity>> {
        return categoryRepository.getAllCategories()
    }
}