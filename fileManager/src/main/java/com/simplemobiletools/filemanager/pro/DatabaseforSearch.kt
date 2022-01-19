package com.simplemobiletools.filemanager.pro

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SearchDatabase::class], version = 1,exportSchema = false)
abstract class DatabaseforSearch : RoomDatabase() {
    abstract fun searchDatabaseDao(): SearchDatabaseDao

    companion object {
        private const val DB_NAME = "Database_for_search"

        @Volatile
        var INSTANCE: DatabaseforSearch? = null
        fun getInstance(context: Context): DatabaseforSearch? {
            synchronized(this) {
            if (INSTANCE == null) {
                INSTANCE =
                    Room.databaseBuilder(context.applicationContext, DatabaseforSearch::class.java, DB_NAME)
                        .allowMainThreadQueries()

                        .build()
            }
                return INSTANCE
            }
        }
    }
}