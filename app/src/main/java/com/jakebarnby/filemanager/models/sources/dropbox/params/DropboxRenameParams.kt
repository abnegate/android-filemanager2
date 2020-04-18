package com.jakebarnby.filemanager.models.sources.dropbox.params

import com.jakebarnby.filemanager.models.RenameParams

class DropboxRenameParams(
    var oldPath: String,
    var newPath: String,
    override var newName: String
): RenameParams