package com.blue.taskflow.domain.repository

import com.blue.taskflow.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    //新增任务
    suspend fun insertTask(task: TaskEntity)

    //删除任务
    suspend fun deleteTaskById(taskId: Long)

    //修改任务
    suspend fun updateTask(task: TaskEntity)
    //suspend fun updateTaskDetail(title: String, description: String)

    //获取全部任务
    fun queryAllTasks(): Flow<List<TaskEntity>>

    //获取具体任务
    suspend fun queryTaskById(taskId: Long): TaskEntity?
}