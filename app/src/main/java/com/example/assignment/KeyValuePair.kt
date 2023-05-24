package com.example.assignment

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "key_value_pairs")
data class KeyValuePair(
    @PrimaryKey val key: String,
    val value: String
)