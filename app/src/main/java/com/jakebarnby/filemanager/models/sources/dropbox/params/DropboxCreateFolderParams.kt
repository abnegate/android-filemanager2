package com.jakebarnby.filemanager.models.sources.dropbox.params

import com.jakebarnby.filemanager.models.CreateFolderParams

data class DropboxCreateFolderParams(
    var path: String,
    override var folderName: String
): CreateFolderParams