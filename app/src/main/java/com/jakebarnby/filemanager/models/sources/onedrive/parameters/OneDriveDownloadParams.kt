package com.jakebarnby.filemanager.models.sources.onedrive

import com.jakebarnby.filemanager.models.DownloadParams

data class OneDriveDownloadParams(
    var id: String,
    override var fileName: String,
    override var destinationPath: String
) : DownloadParams