package com.jakebarnby.filemanager.models.sources.googledrive.params

import com.jakebarnby.filemanager.models.UploadParams

data class GoogleDriveUploadParams(
    var parentId: String,
    override var fileName: String,
    override var filePath: String
): UploadParams