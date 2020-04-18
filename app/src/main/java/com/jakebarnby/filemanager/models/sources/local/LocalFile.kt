package com.jakebarnby.filemanager.models.sources.local

import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceType
import java.io.File

/**
 * Created by Jake on 6/5/2017.
 */
class LocalFile(file: File) : SourceFile() {

    init {
        path = file.path
        name = file.name
        sourceId = SourceType.LOCAL.id
        isDirectory = file.isDirectory
        isHidden = file.name.startsWith(".")
        size = file.length()
        thumbnailLink = file.absolutePath
        modifiedTime = file.lastModified()
    }
}