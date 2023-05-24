package com.example.assignment.roomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface KeyValueDao {
    @Insert
    suspend fun insert(entity: List<KeyValueEntity>)

    @Query("SELECT * FROM key_value_table")
    suspend fun getAll(): List<KeyValueEntity>

    // Add other necessary methods like delete, update, etc.
}