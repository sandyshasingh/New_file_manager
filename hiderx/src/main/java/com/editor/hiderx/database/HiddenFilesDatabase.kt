package com.editor.hiderx.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HiddenFiles::class],version = 4,exportSchema = false)
abstract class HiddenFilesDatabase : RoomDatabase() {
    abstract val hiddenFilesDao : HiddenFilesDao
    companion object
    {
        @Volatile
        private var INSTANCE : HiddenFilesDatabase? = null
        fun getInstance(context: Context): HiddenFilesDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        HiddenFilesDatabase::class.java,
                        "hidden_files_database"
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}