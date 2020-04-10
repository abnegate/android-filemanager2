package com.jakebarnby.filemanager.sources.googledrive

import com.google.api.services.drive.model.File
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.sources.models.SourceType
import com.jakebarnby.filemanager.util.Constants.Sources

/**
 * Created by Jake on 6/5/2017.
 */
class GoogleDriveFile(
    file: File
) : SourceFile() {

    var driveId: String

    init {
        if (file.webContentLink != null) {
            path = file.webContentLink
        }
        driveId = file.id
        name = file.name
        sourceType = SourceType.REMOTE
        sourceName = Sources.GOOGLE_DRIVE
        driveId = file.id
        isDirectory = file.mimeType == Sources.GOOGLE_DRIVE_FOLDER_MIME
        thumbnailLink = if (file.hasThumbnail) file.thumbnailLink else file.iconLink
        modifiedTime = file.modifiedTime.value
        isHidden = file.name.startsWith(".")

        if (!isDirectory) {
            size = if (file.getSize() == null) 0 else file.getSize()
        }
        if (file.webViewLink != null) {
            path = file.webViewLink
        }
    }
}