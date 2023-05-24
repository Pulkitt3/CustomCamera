package com.example.assignment.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "key_value_table")
data class KeyValueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var key: String,
    var value: String
)