package com.jakebarnby.filemanager.models.sources.onedrive

import com.jakebarnby.filemanager.models.RenameParams

class OneDriveRenameParams(
    var driveId: String,
    override var newName: String
): RenameParams