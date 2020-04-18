package com.jakebarnby.filemanager.models.sources.googledrive.params

import com.jakebarnby.filemanager.models.RenameParams

class GoogleDriveRenameParams(
    var fileId: String,
    override var newName: String
): RenameParams