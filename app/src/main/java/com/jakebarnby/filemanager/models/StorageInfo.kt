package com.jakebarnby.filemanager.models

/**
 * Created by Jake on 7/31/2017.
 */
data class StorageInfo(
    var totalSpace: Long = 0,
    var usedSpace: Long = 0,
    var freeSpace: Long = 0
)