package com.jakebarnby.filemanager.models.sources.dropbox.params

import com.jakebarnby.filemanager.models.UploadParams

data class DropboxUploadParams(
    var destinationPath: String,
    override var fileName: String,
    override var filePath: String
): UploadParams