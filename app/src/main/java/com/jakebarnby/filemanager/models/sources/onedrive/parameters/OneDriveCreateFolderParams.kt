package com.jakebarnby.filemanager.models.sources.onedrive

import com.jakebarnby.filemanager.models.CreateFolderParams

data class OneDriveCreateFolderParams(
    var parentId: String,
    override var folderName: String
): CreateFolderParams