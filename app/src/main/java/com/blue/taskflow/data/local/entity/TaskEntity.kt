package com.blue.taskflow.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["category_id"])
    ]
)
data class TaskEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title:String,

    val description: String? = null,                            //具体描述

    val isComplete: Boolean = false,

    val priority: Int? = null,                                  //优先级

    @ColumnInfo("category_id")
    val categoryId: Long? = null,                               //分类

    @ColumnInfo(name = "create_time")
    val createTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "start_time")
    val startTime: Long,

    @ColumnInfo(name = "end_time")
    val endTime: Long,

    @ColumnInfo(name = "is_notify_enabled")
    val isNotifyEnabled: Boolean = false,

    @ColumnInfo(name = "notify_hour")
    val notifyHour: Int? = null,

    @ColumnInfo(name = "notify_minute")
    val notifyMinute: Int? = null
)
