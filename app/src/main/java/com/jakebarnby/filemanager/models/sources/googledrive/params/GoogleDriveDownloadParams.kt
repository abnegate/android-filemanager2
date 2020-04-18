package com.jakebarnby.filemanager.models.sources.googledrive.params

import com.jakebarnby.filemanager.models.DownloadParams

data class GoogleDriveDownloadParams(
    var id: String,
    override var fileName: String,
    override var destinationPath: String
) : DownloadParams