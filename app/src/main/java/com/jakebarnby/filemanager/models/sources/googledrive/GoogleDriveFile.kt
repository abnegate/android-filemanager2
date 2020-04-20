package com.jakebarnby.filemanager.models.sources.googledrive

import com.google.api.services.drive.model.File
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.util.Constants.Sources

/**
 * Created by Jake on 6/5/2017.
 */
class GoogleDriveFile(
    file: File
) : SourceFile() {

    init {
        if (file.webContentLink != null) {
            path = file.webContentLink
        }
        if (file.webViewLink != null) {
            path = file.webViewLink
        }

        id = file.id.hashCode().toLong()
        parentFileId = file.parents?.get(0)?.hashCode()?.toLong() ?: -1L
        remoteFileId = file.id.hashCode().toLong()
        name = file.name
        sourceId = SourceType.GOOGLE_DRIVE.id
        isDirectory = file.mimeType == Sources.GOOGLE_DRIVE_FOLDER_MIME

        thumbnailLink = if (file.hasThumbnail) {
            file.thumbnailLink
        } else {
            file.iconLink
        }
        modifiedTime = file.modifiedTime.value
        isHidden = file.name.startsWith(".")

        if (!isDirectory) {
            size = if (file.getSize() == null) {
                0
            } else {
                file.getSize()
            }
        }
    }
}