package com.jakebarnby.filemanager.models

enum class SourceType(
    val id: Int,
    val sourceName: String
) {
    LOCAL(0,"Local"),
    DROPBOX(1, "Dropbox"),
    GOOGLE_DRIVE(2, "Google Drive"),
    ONEDRIVE(2, "OneDrive")
}