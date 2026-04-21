package com.blue.taskflow.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithTasks(

    @Embedded
    val category: CategoryEntity,                       //父对象

    @Relation(                                          //关联实体
        parentColumn = "id",
        entityColumn = "category_id"
    )
    val tasks: List<TaskEntity>
)
