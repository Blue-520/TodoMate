package com.blue.taskflow.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.blue.taskflow.data.local.dao.CategoryDao
import com.blue.taskflow.data.local.dao.TaskDao
import com.blue.taskflow.data.local.entity.CategoryEntity
import com.blue.taskflow.data.local.entity.TaskEntity

@Database(entities = [TaskEntity::class, CategoryEntity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun taskDao():TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object{
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase{
            return instance ?: synchronized(this){
                instance ?: Room.databaseBuilder(
                    context = context.applicationContext,
                    klass = AppDatabase::class.java,
                    name = "todemate_app_database"
                ).build().also { db ->
                    instance = db
                }
            }
        }
    }
}