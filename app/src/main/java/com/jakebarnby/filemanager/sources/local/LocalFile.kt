package com.jakebarnby.filemanager.sources.local

import com.jakebarnby.filemanager.models.SourceFile
import java.io.File

/**
 * Created by Jake on 6/5/2017.
 */
class LocalFile(file: File, fileSourceId: Int) : SourceFile() {

    init {
        path = file.path
        name = file.name
        sourceId = fileSourceId
        isDirectory = file.isDirectory
        isHidden = file.name.startsWith(".")
        size = file.length()
        thumbnailLink = file.absolutePath
        modifiedTime = file.lastModified()
    }
}