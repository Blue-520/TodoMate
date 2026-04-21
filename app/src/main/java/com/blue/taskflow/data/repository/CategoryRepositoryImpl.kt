package com.blue.taskflow.data.repository

import com.blue.taskflow.data.local.dao.CategoryDao
import com.blue.taskflow.data.local.entity.CategoryEntity
import com.blue.taskflow.data.local.entity.CategoryWithTasks
import com.blue.taskflow.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(private val categoryDao: CategoryDao): CategoryRepository {
    override suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }

    override suspend fun delete(categoryId: Long) {
        categoryDao.deleteCategoryById(categoryId)
    }

    override fun queryAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.queryCategories()
    }

    override fun queryCategoryById(categoryId: Long): Flow<List<CategoryWithTasks>> {
        return categoryDao.queryCategoryWithTasks(categoryId)
    }
}