package com.example.assignment.roomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface KeyValuePairDao {
   @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keyValuePair: KeyValuePair)

    @Query("SELECT value FROM key_value_pairs WHERE `key` = :key")
    suspend fun getValueByKey(key: String): String?
}