package com.jakebarnby.filemanager.models.sources.onedrive

import com.jakebarnby.filemanager.models.UploadParams

data class OneDriveUploadParams(
    var parentId: String,
    override var fileName: String,
    override var filePath: String
): UploadParams