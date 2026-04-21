package com.blue.taskflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.blue.taskflow.data.local.entity.CategoryEntity
import com.blue.taskflow.data.local.entity.CategoryWithTasks
import com.blue.taskflow.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    //增加分类
    @Insert
    suspend fun insertCategory(category: CategoryEntity)

    //删除分类
    @Query("delete from categories where id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

    //查询全部分类
    @Query("select * from categories")
    fun queryCategories(): Flow<List<CategoryEntity>>

    //查询指定分类下的全部任务
    @Transaction
    @Query("select * from categories where id = :categoryId")
    fun queryCategoryWithTasks(categoryId: Long): Flow<List<CategoryWithTasks>>
}