package com.example.assignment

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.assignment.KeyValuePairDao

@Database(entities = [KeyValuePair::class,KeyValueEntity::class], version = 2)
abstract class MyAppDatabase : RoomDatabase() {
    abstract fun keyValuePairDao(): KeyValuePairDao
    abstract fun keyValueDao(): KeyValueDao

    companion object {
        @Volatile
        private var INSTANCE: MyAppDatabase? = null

        fun getDatabase(context: Context): MyAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyAppDatabase::class.java,
                    "my_app_database"
                ).fallbackToDestructiveMigration().build()

                INSTANCE = instance
                instance
            }
        }
    }
}
