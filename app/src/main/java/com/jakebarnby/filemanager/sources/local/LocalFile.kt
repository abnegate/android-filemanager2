package com.jakebarnby.filemanager.sources.local

import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.sources.models.SourceType
import java.io.File

/**
 * Created by Jake on 6/5/2017.
 */
class LocalFile(file: File, fileSourceName: String) : SourceFile() {

    init {
        path = file.path
        name = file.name
        sourceType = SourceType.LOCAL
        sourceName = fileSourceName
        isDirectory = file.isDirectory
        isHidden = file.name.startsWith(".")
        size = file.length()
        thumbnailLink = file.absolutePath
        modifiedTime = file.lastModified()
    }
}