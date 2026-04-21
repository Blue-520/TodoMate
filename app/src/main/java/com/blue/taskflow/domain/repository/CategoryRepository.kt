package com.blue.taskflow.domain.repository

import com.blue.taskflow.data.local.entity.CategoryEntity
import com.blue.taskflow.data.local.entity.CategoryWithTasks
import com.blue.taskflow.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    suspend fun insertCategory(category: CategoryEntity)

    suspend fun delete(categoryId: Long)

    fun queryAllCategories(): Flow<List<CategoryEntity>>

    fun queryCategoryById(categoryId: Long): Flow<List<CategoryWithTasks>>
}