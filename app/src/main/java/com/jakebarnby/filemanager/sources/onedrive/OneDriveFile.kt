package com.jakebarnby.filemanager.sources.onedrive

import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.sources.models.SourceType
import com.jakebarnby.filemanager.util.Constants.Sources
import com.microsoft.graph.extensions.DriveItem

/**
 * Created by Jake on 6/7/2017.
 */
class OneDriveFile(file: DriveItem) : SourceFile() {

    var driveId: String

    init {
        path = file.webUrl
        driveId = file.id
        name = file.name
        sourceType = SourceType.REMOTE
        sourceName = Sources.ONEDRIVE
        isDirectory = file.folder != null
        size = file.size
        modifiedTime = file.lastModifiedDateTime.timeInMillis
        isHidden = file.name.startsWith(".")
        if (file.thumbnails != null && file.thumbnails.currentPage.size > 0) {
            thumbnailLink = file.thumbnails.currentPage[0].small.url
        }
    }
}