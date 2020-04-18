package com.jakebarnby.filemanager.models.sources.onedrive

import androidx.room.Entity
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceType
import com.microsoft.graph.extensions.DriveItem

/**
 * Created by Jake on 6/7/2017.
 */
@Entity
class OneDriveFile(file: DriveItem) : SourceFile() {

    init {
        path = file.webUrl
        fileId = file.id.hashCode().toLong()
        remoteFileId = file.id.hashCode().toLong()
        name = file.name
        sourceId = SourceType.ONEDRIVE.id
        isDirectory = file.folder != null
        size = file.size
        modifiedTime = file.lastModifiedDateTime.timeInMillis
        isHidden = file.name.startsWith(".")
        if (file.thumbnails != null && file.thumbnails.currentPage.size > 0) {
            thumbnailLink = file.thumbnails.currentPage[0].small.url
        }
    }
}