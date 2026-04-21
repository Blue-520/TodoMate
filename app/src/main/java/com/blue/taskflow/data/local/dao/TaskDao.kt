package com.blue.taskflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.blue.taskflow.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    //增加任务
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: TaskEntity)

    //删除任务
    @Query("delete from tasks where id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    //修改任务
    @Update
    suspend fun update(task: TaskEntity)

    @Query("update tasks set title = :title, description = :description")
    suspend fun updateTaskById(title: String, description: String)

    //查询任务
    @Query("select * from tasks where id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Query("select * from tasks order by create_time desc")
    fun queryAllTasks(): Flow<List<TaskEntity>>
}