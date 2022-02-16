package com.editor.hiderx.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HiddenFilesDao {


    @Insert(entity = HiddenFiles::class,onConflict = OnConflictStrategy.REPLACE)
    fun  insertFile(hiddenFile: HiddenFiles)





    @Query("select exists (select 1 from HiddenFiles where path =:path)")
    fun isFileExists(path : String) : Boolean

    @Query("select originalPath from HiddenFiles where path =:path")
    fun getOriginalPathForFile(path : String) : String?

    @Query("select * from HiddenFiles where type like :mType order by updateTime desc")
    fun getAllFiles(mType : String?) : List<HiddenFiles>?



    @Query("select updateTime from HiddenFiles where path =:path")
    fun getUpdateTimeForFile(path : String) : Long?

    @Query("update HiddenFiles set path =:newPath , updateTime = :updateTime where path =:path")
    fun updateFilePath(path : String, newPath : String,updateTime : Long?) : Int

    @Query("delete from HiddenFiles where path =:path")
    fun deleteFile(path: String?)

}