package com.jakebarnby.filemanager.models.sources.googledrive.params

import com.jakebarnby.filemanager.models.CreateFolderParams

data class GoogleDriveCreateFolderParams(
    var parentId: String,
    override var folderName: String
): CreateFolderParams