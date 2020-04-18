package com.jakebarnby.filemanager.models.sources.dropbox.params

import com.jakebarnby.filemanager.models.DownloadParams

data class DropboxDownloadParams(
    var id: String,
    override var fileName: String,
    override var destinationPath: String
) : DownloadParams