package com.blue.taskflow.data.repository

import com.blue.taskflow.data.local.dao.TaskDao
import com.blue.taskflow.data.local.entity.TaskEntity
import com.blue.taskflow.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class TaskRepositoryImpl(private val taskDao: TaskDao): TaskRepository {

    override suspend fun insertTask(task: TaskEntity) {
        return taskDao.insertTask(task)
    }

    override suspend fun deleteTaskById(taskId: Long) {
        return taskDao.deleteTaskById(taskId)
    }

    override suspend fun updateTask(task: TaskEntity) {
        taskDao.update(task)
    }

    override fun queryAllTasks(): Flow<List<TaskEntity>> {
        return taskDao.queryAllTasks()
    }

    override suspend fun queryTaskById(taskId: Long): TaskEntity? {
        return taskDao.queryTaskById(taskId)
    }
}