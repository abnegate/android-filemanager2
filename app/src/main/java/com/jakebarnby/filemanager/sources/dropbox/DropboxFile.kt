package com.jakebarnby.filemanager.sources.dropbox

import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.Metadata
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.sources.models.SourceType
import com.jakebarnby.filemanager.util.Constants.Sources

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
        sourceType = SourceType.REMOTE
        sourceName = Sources.DROPBOX
        isDirectory = file is FolderMetadata
        isHidden = file.name.startsWith(".")
    }
}