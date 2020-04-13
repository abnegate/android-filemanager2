package com.jakebarnby.filemanager.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jakebarnby.filemanager.models.SourceFile

@Database(
    version = 1,
    entities = [
        SourceFile::class
    ]
)
abstract class FileDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
}