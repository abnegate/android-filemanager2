package com.jakebarnby.filemanager.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.jakebarnby.filemanager.models.SourceFile

@Dao
interface FileDao {

    @Insert
    fun insertAll(vararg files: SourceFile)

    @Query("SELECT * FROM SourceFile")
    suspend fun getAll(): List<SourceFile>

    @Query("SELECT * FROM SourceFile")
    fun getAllLiveData(): LiveData<List<SourceFile>>

    @Query("SELECT * FROM SourceFile WHERE sourceId = :sourceId")
    suspend fun findBySource(sourceId: Int): List<SourceFile>

    @Query("SELECT * FROM SourceFile WHERE sourceId = :sourceId")
    fun findBySourceLiveData(sourceId: Int): LiveData<List<SourceFile>>

    @Query("SELECT * FROM SourceFile WHERE parentFileId = :parentId")
    suspend fun findByParentId(parentId: Long): List<SourceFile>

    @Query("SELECT * FROM SourceFile WHERE parentFileId = :parentId")
    fun findByParentIdLiveData(parentId: Long): LiveData<List<SourceFile>>

    @Update
    fun update(file: SourceFile)

    @Update
    fun update(vararg files: SourceFile)

    @Delete
    fun delete(file: SourceFile)
}