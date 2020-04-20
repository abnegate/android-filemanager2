package com.jakebarnby.filemanager.models.sources.dropbox

import androidx.room.Entity
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.Metadata
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceType
import java.io.File

/**
 * Created by Jake on 6/5/2017.
 */
class DropboxFile(file: Metadata) : SourceFile() {

    init {
        path = if (file.pathDisplay != null) {
            file.pathDisplay
        } else {
            ""
        }
        if (file is FileMetadata) {
            size = file.size
        }
        name = file.name
        id = file.pathDisplay.hashCode().toLong()
        parentFileId = file.pathDisplay.substring(0..file.pathDisplay.lastIndexOf(File.separatorChar)).hashCode().toLong()
        sourceId = SourceType.DROPBOX.id
        isDirectory = file is FolderMetadata
        isHidden = file.name.startsWith(".")
    }
}