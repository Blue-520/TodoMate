package com.blue.taskflow.data.reposity

import com.blue.taskflow.data.local.dao.TaskDao
import com.blue.taskflow.data.local.entity.TaskEntity
import com.blue.taskflow.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class TaskRepositoryImpl(private val taskDao: TaskDao): TaskRepository {
    override fun queryAllTasks(): Flow<List<TaskEntity>> {
        return taskDao.queryAllTasks()
    }

    override suspend fun deleteTask(taskId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun getTaskById(taskId: Long): TaskEntity? {
        return taskDao.getTaskById(taskId)
    }

    override suspend fun insertTask(task: TaskEntity) {
        return taskDao.insertTask(task)
    }

    override suspend fun updateTask(task: TaskEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun updateTaskDetail(title: String, description: String) {
        TODO("Not yet implemented")
    }
}