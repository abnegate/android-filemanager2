package com.jakebarnby.filemanager.models.sources.local

import com.jakebarnby.filemanager.models.RenameParams

class LocalRenameParams(
    var path: String,
    var oldName: String,
    override var newName: String
): RenameParams