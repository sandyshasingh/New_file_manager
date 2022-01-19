package com.simplemobiletools.filemanager.pro

import androidx.room.*

@Dao
interface SearchDatabaseDao {


    @Query("DELETE FROM DatabaseforSearch where mPath =:id ")
    fun deleteSearchResult(id:String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSearchResult(searchDatabase: SearchDatabase)

    @Query("SELECT * FROM DatabaseforSearch where mName LIKE :parameter ")
    fun getSearchResult(parameter : String): List<SearchDatabase>?

    @Query("DELETE FROM DatabaseforSearch ")
    fun deleteAll()
}