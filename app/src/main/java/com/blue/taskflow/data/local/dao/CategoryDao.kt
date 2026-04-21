package com.blue.taskflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.blue.taskflow.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun addCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("select * from categories")
    suspend fun queryCategories(categories: List<CategoryEntity>): Flow<List<CategoryEntity>>

    @Query("select * from categories where id = :categoryId")
    suspend fun queryCategory(categoryId: Long): Flow<CategoryEntity>
}