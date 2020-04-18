package com.jakebarnby.filemanager.models.sources.local

import com.jakebarnby.filemanager.models.CreateFolderParams

data class LocalCreateFolderParams(
    val path: String,
    override var folderName: String
): CreateFolderParams